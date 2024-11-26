package com.my.firstbeat.web.service;

import com.github.benmanes.caffeine.cache.Cache;
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

    private Long userId;

    @BeforeEach
    void setUp(){
        testUser = mockUser();
        userId = 1L;
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

        given(userService.findByIdOrFail(userId)).willReturn(testUser);
        given(recommendationCache.get(eq(userId), any())).willReturn(recommendations);
        given(trackRepository.existsInUserPlaylist(testUser, response.getSpotifyTrackId())).willReturn(false);

        //when
        TrackRecommendationResponse result = recommendationService.getRecommendations(userId);

        assertThat(result).isEqualTo(response);
        verify(spotifyClient, never()).getRecommendations(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("추천 트랙 조회: 캐시에 데이터가 부족할 시 새로운 추천 트랙 리스트 요청")
    void getRecommendations_refresh_when_cache_insufficient(){

        Queue<TrackRecommendationResponse> emptyCache = new ConcurrentLinkedQueue<>();
        List<Genre> genreList = Arrays.asList(
                new Genre("k-pop"),
                new Genre("dance")
        );
        List<Track> trackList = Arrays.asList(
                Track.builder().name("Steady").spotifyTrackId("spotifyTrackId 112").build(),
                Track.builder().name("Supercute").spotifyTrackId("spotifyTrackId 111").build()
        );

        RecommendationResponse mockRecommendation = new RecommendationResponse();
        List<TrackResponse> tracks = IntStream.range(0, 20)
                .mapToObj(i -> builder()
                        .id("spotifyId: "+i)
                        .artists(new TrackResponse.ArtistResponse("nct wish"))
                        .build())
                .collect(Collectors.toList());

        mockRecommendation.setTracks(tracks);

        given(userService.findByIdOrFail(userId)).willReturn(testUser);

        //첫 번째 캐시 접근
        given(recommendationCache.get(anyLong(), any())).willReturn(emptyCache);

        //두 번째 캐시 접근
        //given(recommendationCache.get)

        given(genreRepository.findTop5GenresByUser(any(User.class), any(Pageable.class))).willReturn(genreList);
        given(playlistRepository.findAllTrackByUser(any(User.class), any(Pageable.class))).willReturn(trackList);
        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .willAnswer(invocation -> {
                    RecommendationResponse response = new RecommendationResponse();
                    response.setTracks(tracks);
                    return response;
                });
        given(trackRepository.existsInUserPlaylist(any(User.class), anyString())).willReturn(false);


        //when
        TrackRecommendationResponse result = recommendationService.getRecommendations(userId);

        verify(spotifyClient, times(1)).getRecommendations(anyString(), anyString(), anyInt());
        assertThat(result).isNotNull();
    }



}