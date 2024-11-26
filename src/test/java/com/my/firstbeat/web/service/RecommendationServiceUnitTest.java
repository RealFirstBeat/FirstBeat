package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
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
import org.springframework.test.context.ActiveProfiles;

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

    private User testUser;

    private Long userId;

    @BeforeEach
    void setUp(){
        testUser = mockUser();
        userId = 1L;
    }

//    @Test
//    @DisplayName("추천 트랙 조회: 캐시에 충분한 추천곡이 있고, 플레이리스트에 없는 경우 즉시 반환")
//

}