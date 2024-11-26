package com.my.firstbeat.web.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;
import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.TrackResponse.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RecommendationServiceUnitTest extends DummyObject {

    @InjectMocks
    private RecommendationService recommendationService;

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

    @Mock
    private Cache<Long, Queue<TrackRecommendationResponse>> recommendationCache;

    private User testUser;


    @BeforeEach
    void setUp(){
        testUser = mockUserWithId();
    }

    @Test
    @DisplayName("추천 트랙 조회: 캐시에 충분한 추천곡이 있고, 플레이리스트에 없는 경우 즉시 반환")
    void getRecommendations_with_sufficientCache_and_not_in_playlist(){

        TrackRecommendationResponse response = new TrackRecommendationResponse(TrackResponse.builder()
                .trackName("트랙 이름")
                .id("spotifyTrackId")
                .artists(new TrackResponse.ArtistResponse("아티스트 이름"))
                .isPlayable(true)
                .previewUrl("프리뷰 url")
                .build());

        Queue<TrackRecommendationResponse> recommendations = new ConcurrentLinkedQueue<>();
        recommendations.offer(response);

        given(userService.findByIdOrFail(testUser.getId())).willReturn(testUser);
        given(recommendationCache.get(eq(testUser.getId()), any())).willReturn(recommendations);
        given(trackRepository.existsInUserPlaylist(testUser, response.getSpotifyTrackId())).willReturn(false);

        //when
        TrackRecommendationResponse result = recommendationService.getRecommendations(testUser.getId());

        assertThat(result).isEqualTo(response);
        verify(spotifyClient, never()).getRecommendations(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("추천 트랙 조회: 캐시에 데이터가 부족할 시 새로운 추천 트랙 리스트 요청")
    void getRecommendations_refresh_when_cache_insufficient(){
        List<Genre> genreList = Arrays.asList(
                new Genre("k-pop"),
                new Genre("dance")
        );
        List<Track> trackList = Arrays.asList(
                Track.builder().name("Steady").spotifyTrackId("spotifyTrackId 112").build(),
                Track.builder().name("Supercute").spotifyTrackId("spotifyTrackId 111").build()
        );

        List<TrackResponse> tracks = IntStream.range(0, 20)
                .mapToObj(i -> TrackResponse.builder()
                        .id("spotifyId: "+i)
                        .name("spotify name: "+i)
                        .trackName("track: "+i)
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build())
                .collect(Collectors.toList());

        RecommendationResponse mockRecommendation = new RecommendationResponse();
        mockRecommendation.setTracks(tracks);

        given(userService.findByIdOrFail(testUser.getId())).willReturn(testUser);

        Queue<TrackRecommendationResponse> sharedQueue = new ConcurrentLinkedQueue<>();
        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt())).willAnswer(invocation -> {
            //spotify api 호출하면서 큐에 데이터 추가
            tracks.stream()
                    .map(TrackRecommendationResponse::new)
                    .forEach(sharedQueue::offer);
            return mockRecommendation;
        });


        //처음에는 빈 큐 반환하고, 그 이후 호출에서는 데이터가 채워진 큐를 반환하도록
        given(recommendationCache.get(eq(testUser.getId()), any())).willAnswer(invocation -> {
            if(sharedQueue.isEmpty()){ //첫 번째 캐시 접근 시 -> 빈 데이터 이므로 빈 객체 반환
                return new ConcurrentLinkedQueue<>();
            }
            return sharedQueue; //이후 두 번째 접근에서는 데이터가 있는 sharedQueue 를 반환
        });

        given(genreRepository.findTop5GenresByUser(eq(testUser), any(Pageable.class))).willReturn(genreList);
        given(playlistRepository.findAllTrackByUser(eq(testUser), any(Pageable.class))).willReturn(trackList);
        given(trackRepository.existsInUserPlaylist(eq(testUser), anyString())).willReturn(false);

        //when
        TrackRecommendationResponse result = recommendationService.getRecommendations(testUser.getId());

        verify(spotifyClient, times(1)).getRecommendations(anyString(), anyString(), anyInt());
        verify(recommendationCache, atLeastOnce()).get(anyLong(), any());
        assertThat(result.getSpotifyTrackId()).isNotNull();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("추천 트랙 조회: 캐시에 데이터가 있지만 모두 플레이리스트에 존재하는 경우 새로운 추천")
    void getRecommendation_when_all_cached_tracks_in_playlist(){
        List<Genre> genreList = Arrays.asList(
                new Genre("k-pop"),
                new Genre("dance")
        );

        Queue<TrackRecommendationResponse> initalQueue = new ConcurrentLinkedQueue<>();
        TrackRecommendationResponse cachedTrack = new TrackRecommendationResponse(
                TrackResponse.builder()
                        .id("캐시된 트랙 id")
                        .trackName("캐시된 트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build()
        );
        initalQueue.offer(cachedTrack);

        List<TrackResponse> newTrack = List.of(
                TrackResponse.builder()
                        .id("새 트랙 id")
                        .trackName("새 트랙 이름")
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build());
        RecommendationResponse mockRecommendation = new RecommendationResponse();
        mockRecommendation.setTracks(newTrack);

        given(userService.findByIdOrFail(testUser.getId())).willReturn(testUser);
        given(recommendationCache.get(eq(testUser.getId()), any())).willReturn(initalQueue);
        given(trackRepository.existsInUserPlaylist(eq(testUser), eq("캐시된 트랙 id"))).willReturn(true);
        given(genreRepository.findTop5GenresByUser(any(), any())).willReturn(genreList);
        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt())).willReturn(mockRecommendation);
        given(trackRepository.existsInUserPlaylist(eq(testUser), eq("새 트랙 id"))).willReturn(false);

        //when
        TrackRecommendationResponse results = recommendationService.getRecommendations(testUser.getId());

        assertThat(results.getSpotifyTrackId()).isEqualTo("새 트랙 id");
        verify(spotifyClient, times(1)).getRecommendations(anyString(), anyString(), anyInt());
    }


    @Test
    @DisplayName("캐시 리프레시 조건 확인")
    void needsRefresh_should_return_true_when_less_than_threshold(){
        ConcurrentLinkedQueue<TrackRecommendationResponse> queue = new ConcurrentLinkedQueue<>();
        assertThat(recommendationService.needsRefresh(queue)).isTrue();
    }



}