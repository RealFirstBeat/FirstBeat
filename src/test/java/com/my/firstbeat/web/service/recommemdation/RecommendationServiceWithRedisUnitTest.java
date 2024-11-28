package com.my.firstbeat.web.service.recommemdation;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import com.my.firstbeat.web.service.UserService;
import com.my.firstbeat.web.service.recommemdation.lock.RedisLockManager;
import com.my.firstbeat.web.service.recommemdation.property.LockProperties;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceWithRedisUnitTest extends DummyObject {

    @Mock
    private SpotifyClient spotifyClient;
    @Mock
    private UserService userService;
    @Mock
    private TrackRepository trackRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ListOperations<String, Object> listOperations;
    @Mock
    private RedisLockManager lockManager;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private RecommendationServiceWithRedis recommendationService;

    private RecommendationProperties properties;

    private User testUser;
    private static final String REDIS_KEY = "recommendation:user:1";
    private static final String FAILED_KEY = "recommendation:failed-refresh";

    @BeforeEach
    void setUp() {
        properties = new RecommendationProperties();
        properties.setMaxAttempts(3);
        properties.setRefreshThreshold(5);
        properties.getRedis().setKeyPrefix("recommendation:user:");
        properties.getRedis().setCacheTtlHours(24);
        properties.getRedis().setFailedTasksKey("recommendation:failed-refresh");
        ReflectionTestUtils.setField(recommendationService, "properties", properties);


        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("test-recommendation-refresh-");
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100);
        executor.initialize();
        ReflectionTestUtils.setField(recommendationService, "recommendationBackgroundExecutor",
                executor.getThreadPoolExecutor());


        testUser = mockUserWithId();

        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("추천 트랙 반환: 캐시된 추천이 있고 플레이리스트에 없을 때 즉시 반환")
    void getRecommendations_should_return_CachedRecommendation_immediately() {
        TrackRecommendationResponse cached =
                new TrackRecommendationResponse(TrackResponse.builder()
                        .id("spotifyTrackId")
                        .trackName("트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("아티스트 이름"))
                        .previewUrl("previewURL")
                        .albumCoverUrl("albumCoverUrl")
                        .build());

        when(userService.findByIdOrFail(any(Long.class))).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(15L);
        when(listOperations.leftPop(REDIS_KEY)).thenReturn(cached);
        when(trackRepository.existsInUserPlaylist(testUser, "spotifyTrackId")).thenReturn(false);

        TrackRecommendationResponse result = recommendationService.getRecommendations(testUser.getId());

        assertThat(result).isEqualTo(cached);
        verify(lockManager, never()).executeWithLockWithRetry(any(), any());
    }


    @Test
    @DisplayName("추천 트랙 반환: 캐시 히트지만 플레이리스트에 있는 곡이어서 락 획득 후 다음 추천곡 반환 (캐시에 임계값 이상의 데이터 존재)")
    void getRecommendations_should_refresh_and_return_newRecommendation_on_cache_miss(){
        TrackRecommendationResponse firstRecommendation =
                new TrackRecommendationResponse(TrackResponse.builder()
                        .id("first-spotifyTrackId")
                        .trackName("트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("아티스트 이름"))
                        .previewUrl("previewURL")
                        .albumCoverUrl("albumCoverUrl")
                        .build());

        TrackRecommendationResponse secondRecommendation =
                new TrackRecommendationResponse(TrackResponse.builder()
                        .id("second-spotifyTrackId")
                        .trackName("트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("아티스트 이름"))
                        .previewUrl("previewURL")
                        .albumCoverUrl("albumCoverUrl")
                        .build());

        when(userService.findByIdOrFail(anyLong())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY))
                .thenReturn(15L)
                .thenReturn(14L);

        when(listOperations.leftPop(REDIS_KEY)).thenReturn(firstRecommendation).thenReturn(secondRecommendation);
        when(trackRepository.existsInUserPlaylist(testUser, "first-spotifyTrackId")).thenReturn(true);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(15L);
        when(trackRepository.existsInUserPlaylist(testUser, "second-spotifyTrackId")).thenReturn(false);
        when(lockManager.executeWithLockWithRetry(eq(testUser.getId()), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.of(supplier.get());
                });

        TrackRecommendationResponse result = recommendationService.getRecommendations(testUser.getId());

        assertThat(result).isEqualTo(secondRecommendation);
        verify(spotifyClient, never()).getRecommendations(anyString(), anyString(), anyInt());
        verify(lockManager, times(1)).executeWithLockWithRetry(eq(testUser.getId()), any());
    }


    @Test
    @DisplayName("추천 트랙 반환: 캐시가 임계치 이하로 남았을 때 갱신 후 반환")
    void getRecommendations_should_refresh_when_cache_below_threshold() {
        TrackRecommendationResponse newRecommendation =
                new TrackRecommendationResponse(TrackResponse.builder()
                        .id("spotifyTrackId")
                        .trackName("트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("아티스트 이름"))
                        .previewUrl("previewURL")
                        .albumCoverUrl("albumCoverUrl")
                        .build());

        List<TrackResponse> tracks = IntStream.range(0, 20)
                .mapToObj(i -> TrackResponse.builder()
                        .id("spotifyId: "+i)
                        .name("spotify name: "+i)
                        .trackName("track: "+i)
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build())
                .collect(Collectors.toList());

        RecommendationResponse recommendations = new RecommendationResponse();
        recommendations.setTracks(tracks);

        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY))
                .thenReturn(5L);
        given(genreRepository.findTop5GenresByUser(any(), any())).willReturn(
                List.of(new Genre("pop"), new Genre("k-pop")));
        given(playlistRepository.findAllTrackByUser(any(), any(Pageable.class)))
                .willReturn(List.of(Track.builder().spotifyTrackId("test1").build(),
                        Track.builder().spotifyTrackId("test2").build()));
        when(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .thenReturn(recommendations);
        when(listOperations.leftPop(REDIS_KEY)).thenReturn(newRecommendation);
        when(trackRepository.existsInUserPlaylist(testUser, newRecommendation.getSpotifyTrackId())).thenReturn(false);
        when(lockManager.executeWithLockWithRetry(eq(testUser.getId()), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.of(supplier.get());
                });



        TrackRecommendationResponse result = recommendationService.getRecommendations(testUser.getId());

        assertThat(result).isEqualTo(newRecommendation);
        verify(spotifyClient, times(1)).getRecommendations(anyString(), anyString(), anyInt());
        verify(lockManager, times(1)).executeWithLockWithRetry(eq(testUser.getId()), any());
    }


    @Test
    @DisplayName("추천 트랙 반환: 최대 시도 횟수 초과시 예외 발생")
    void getRecommendations_should_throw_exception_when_exceeds_max_attempts() {
        TrackRecommendationResponse recommendation = new TrackRecommendationResponse(
                TrackResponse.builder()
                        .id("spotifyTrackId")
                        .trackName("Far Away")
                        .artists(new TrackResponse.ArtistResponse("NCT WISH"))
                        .build());

        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(25L);
        when(listOperations.leftPop(REDIS_KEY)).thenReturn(recommendation);
        when(trackRepository.existsInUserPlaylist(testUser, recommendation.getSpotifyTrackId()))
                .thenReturn(true);

        assertThatThrownBy(() -> recommendationService.getRecommendations(testUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAX_RECOMMENDATION_ATTEMPTS_EXCEED);

        verify(listOperations, times(4)).leftPop(REDIS_KEY);
        verify(redisTemplate.opsForList(), times(4)).leftPop(REDIS_KEY);
        verify(trackRepository, times(4)).existsInUserPlaylist(eq(testUser), anyString());

    }

    @Test
    @DisplayName("추천 트랙 갱신: 장르가 없을 경우 예외 발생")
    void refreshRecommendations_should_throw_exception_when_no_genres() {
        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(5L); //임계치
        when(genreRepository.findTop5GenresByUser(eq(testUser), any(Pageable.class))).thenReturn(Collections.emptyList());
        when(lockManager.executeWithLockWithRetry(eq(testUser.getId()), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.of(supplier.get());
                });

        assertThatThrownBy(() -> recommendationService.getRecommendations(testUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENRES_NOT_FOUND);
    }

    @Test
    @DisplayName("추천 트랙 갱신: Spotify API 호출 실패 시 예외 전파")
    void refreshRecommendations_should_propagate_spotify_api_exception() {
        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(5L); //임계치
        when(genreRepository.findTop5GenresByUser(any(), any())).thenReturn(
                List.of(new Genre("pop"), new Genre("k-pop")));
        when(playlistRepository.findAllTrackByUser(any(), any(Pageable.class)))
                .thenReturn(List.of(Track.builder().spotifyTrackId("test1").build(),
                        Track.builder().spotifyTrackId("test2").build()));
        when(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .thenThrow(new SpotifyApiException(new SpotifyWebApiException("api error")));
        when(lockManager.executeWithLockWithRetry(eq(testUser.getId()), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.of(supplier.get());
                });

        assertThatThrownBy(() -> recommendationService.getRecommendations(testUser.getId()))
                .isInstanceOf(SpotifyApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", com.my.firstbeat.client.spotify.ex.ErrorCode.API_ERROR);
    }

    @Test
    @DisplayName("추천 트랙 반환: 락 획득 실패시 서비스 혼잡 예외 발생")
    void getRecommendations_should_throw_exception_when_fail_to_acquire_lock() {
        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(redisTemplate.opsForList().size(REDIS_KEY)).thenReturn(5L);
        when(lockManager.executeWithLockWithRetry(eq(testUser.getId()), any()))
                .thenReturn(Optional.empty()); // 락 획득에 실패

        assertThatThrownBy(() -> recommendationService.getRecommendations(testUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SERVICE_TEMPORARY_UNAVAILABLE);
    }


    @Test
    @DisplayName("백그라운드 갱신: 실패한 작업 재시도 성공")
    void backgroundRefresh_should_retry_failed_tasks_successfully() {
        List<TrackResponse> tracks = IntStream.range(0, 20)
                .mapToObj(i -> TrackResponse.builder()
                        .id("spotifyId: "+i)
                        .name("spotify name: "+i)
                        .trackName("track: "+i)
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build())
                .collect(Collectors.toList());

        RecommendationResponse recommendations = new RecommendationResponse();
        recommendations.setTracks(tracks);

        SetOperations<String, Object> setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        //실패한 작업
        Set<Object> failedTasks = Set.of(testUser.getId().toString());
        when(redisTemplate.opsForSet().members(FAILED_KEY)).thenReturn(failedTasks);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        when(userService.findByIdOrFail(testUser.getId())).thenReturn(testUser);
        when(lockManager.executeWithLockForBackground(eq(testUser.getId()), any()))
                .thenAnswer(invocation -> {
                    Runnable runnable = invocation.getArgument(1);
                    runnable.run();
                    return true;
                });
        when(genreRepository.findTop5GenresByUser(any(), any())).thenReturn(
                List.of(new Genre("pop"), new Genre("k-pop")));
        when(playlistRepository.findAllTrackByUser(any(), any(Pageable.class)))
                .thenReturn(List.of(Track.builder().spotifyTrackId("test1").build(),
                        Track.builder().spotifyTrackId("test2").build()));
        when(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .thenReturn(recommendations);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        recommendationService.backgroundRefresh();

        verify(lockManager).executeWithLockForBackground(eq(testUser.getId()), any());
        verify(redisTemplate.opsForSet()).members(FAILED_KEY);
        verify(spotifyClient).getRecommendations(anyString(), anyString(), anyInt());
        verify(genreRepository).findTop5GenresByUser(any(), any());
        verify(playlistRepository).findAllTrackByUser(any(), any(Pageable.class));
        verify(redisTemplate.opsForList(), times(20)).rightPush(eq(REDIS_KEY), any());
    }



}