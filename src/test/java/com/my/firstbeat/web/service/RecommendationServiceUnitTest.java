package com.my.firstbeat.web.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.my.firstbeat.client.spotify.SpotifyClient;
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

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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



}