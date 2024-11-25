package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.userGenre.UserGenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TrackServiceTest {

    @Autowired
    private SpotifyClient spotifyClient;
    @Autowired
    private UserService userService;
    @Autowired
    private UserGenreRepository userGenreRepository;
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private TrackRepository trackRepository;

    @Test
    @DisplayName("동일 유저에 대한 동시 추천 요청 테스트")
    void getRecommendations_concurrency_test(){

        int threads = 5;
        Long userId = 1L;


    }


}