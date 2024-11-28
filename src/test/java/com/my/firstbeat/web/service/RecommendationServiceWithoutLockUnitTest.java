package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithoutLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;
import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.TrackResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RecommendationServiceWithoutLockUnitTest extends DummyObject {

    @InjectMocks
    private RecommendationServiceWithoutLock recommendationService;

    @Mock
    private SpotifyClient spotifyClient;

    @Mock
    private UserService userService;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private TrackRepository trackRepository;

    private User testUser;
    private static final int CONCURRENT_USERS = 10;
    private static final int REQUESTS_PER_USER = 5;
    private static final int SEED_MAX = 5;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@naver.com", Role.USER);
        given(userService.findByIdOrFail(anyLong())).willReturn(testUser);

        given(trackRepository.existsInUserPlaylist(any(), anyString())).willReturn(false);
        given(genreRepository.findTop5GenresByUser(any(), any())).willReturn(
                List.of(new Genre("pop"), new Genre("k-pop")));
        given(playlistRepository.findAllTrackByUser(any(), any(Pageable.class)))
                .willReturn(List.of(Track.builder().spotifyTrackId("test1").build(),
                        Track.builder().spotifyTrackId("test2").build()));


    }

    @Test
    @DisplayName("여러 사용자가 동시에 추천 요청 테스트: 여러 스레드가 동시에 캐시에 접근할 때 안전한지 확인")
    void getRecommendations_with_multiple_threads_requests() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS); //10개
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS * REQUESTS_PER_USER); //10*5 = 50번
        List<Future<TrackRecommendationResponse>> futures = new ArrayList<>(); //비동기 결과 저장해야 함
        Set<String> recommendedTracks = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger requestCount = new AtomicInteger(0);


        RecommendationResponse mockRecommendation = new RecommendationResponse();
        List<TrackResponse> tracks = IntStream.range(0, 20)
                .mapToObj(i -> builder()
                        .id("spotifyId: "+i)
                        .artists(new ArtistResponse("nct wish"))
                        .build())
                .collect(Collectors.toList());

        mockRecommendation.setTracks(tracks);

        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .willAnswer(invocation -> {
                    RecommendationResponse response = new RecommendationResponse();
                    response.setTracks(tracks);
                    return response;
                });

        for(int user = 0; user < CONCURRENT_USERS; user++){
            final int userId = user;
            for(int request = 0; request<REQUESTS_PER_USER; request++){
                final int requestId = request;
                futures.add(executorService.submit(() -> {
                    try{

                        int currentRequest = requestCount.incrementAndGet();
                        String threadName = Thread.currentThread().getName();
                        System.out.printf("[%s] 요청 시작 (user: %d, request: %d, 전체요청: %d)%n",
                                threadName, userId, requestId, currentRequest);


                        TrackRecommendationResponse response = recommendationService.getRecommendations(testUser.getId());
                        if (response != null) {
                            recommendedTracks.add(response.getSpotifyTrackId());
                            System.out.printf("[%s] 요청 완료 - trackId: %s (user: %d, request: %d, 전체요청: %d)%n",
                                    threadName, response.getSpotifyTrackId(), userId, requestId, currentRequest);
                        }
                        return response;
                    }finally {
                        latch.countDown();
                    }
                }));
            }
        }

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();

        List<TrackRecommendationResponse> results = new ArrayList<>();
        for (Future<TrackRecommendationResponse> future : futures) {
            try {
                TrackRecommendationResponse response = future.get(1, TimeUnit.SECONDS);
                if (response != null) {
                    results.add(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Future.get() 실행 중 에러 발생", e);
            }
        }

        //요청은 누락되면 안됨
        assertThat(results)
                .hasSize(CONCURRENT_USERS * REQUESTS_PER_USER)
                .doesNotContainNull();


        //최소 3번은 spotifyClient 에 추천 리스트 조회 요청 보내야 함
        int expectedMinRefreshes = (CONCURRENT_USERS * REQUESTS_PER_USER) / 20 + 1;
        verify(spotifyClient, atLeast(expectedMinRefreshes)).getRecommendations(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("여러 스레드가 동시에 캐시 리프레시 시도할 때 중복 API 호출 발생 테스트")
    void getRecommendations_with_concurrent_refresh_requests_should_cause_duplicatedRefresh() throws InterruptedException {

        int concurrentRequests = 50;
        int expectedMinimumApiCalls = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);
        AtomicInteger apiCallCount = new AtomicInteger(0); //api 호출 카운트


        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .willAnswer(invocation -> {

                    log.info("Spotify API 호출: {}", System.currentTimeMillis());
                    apiCallCount.incrementAndGet();

                    Thread.sleep(100);

                    RecommendationResponse response = new RecommendationResponse();

                    List<TrackResponse> tracks = IntStream.range(0, 20)
                            .mapToObj(i -> TrackResponse.builder()
                                    .id("spotifyId: "+i)
                                    .artists(new ArtistResponse("nct wish"))
                                    .build())
                            .collect(Collectors.toList());
                    response.setTracks(tracks);
                    return response;
                });


        //캐시 초기화를 위해 첫 번째 호출 수행
        recommendationService.getRecommendations(testUser.getId());


        //여러 스레드에서 동시에 요청
        List<Future<TrackRecommendationResponse>> futures = new ArrayList<>();
        for(int i = 0;i<concurrentRequests; i++){
            futures.add(executorService.submit(() ->{
                startLatch.await();
                try{
                    return recommendationService.getRecommendations(testUser.getId());
                }finally {
                    completionLatch.countDown();
                }
            }));

            if (i % 5 == 0) {
                Thread.sleep(10);
            }
        }

        startLatch.countDown(); // 동시 요청 시작
        completionLatch.await(10, TimeUnit.SECONDS);


        List<TrackRecommendationResponse> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        fail("Future 실행 중 예외 발생: " + e.getMessage());
                        return null;
                    }
                })
                .toList();

        int actualApiCalls = apiCallCount.get();
        log.info("기대한 최소 API 호출 수: {}, 실제 발생한 API 호출 수: {}", expectedMinimumApiCalls, actualApiCalls);


        assertAll(
                () -> assertThat(results).hasSize(concurrentRequests), //모든 요청 성공
                () -> {

                    //중복 발생했는지
                    assertThat(actualApiCalls)
                            .as("api 호출 기대값: %d. 경쟁 상태로 인해 API 중복 호출 발생: %d ", expectedMinimumApiCalls, actualApiCalls)
                            .isGreaterThan(expectedMinimumApiCalls)
                            .isLessThan(concurrentRequests);
                },
                () -> verify(spotifyClient, atLeast(expectedMinimumApiCalls + 1))
                        .getRecommendations(anyString(), anyString(), anyInt())
        );


        executorService.shutdown();
    }





}