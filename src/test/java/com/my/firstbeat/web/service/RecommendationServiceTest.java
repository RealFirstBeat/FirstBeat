package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;
import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.TrackResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceTest extends DummyObject {

    @Autowired
    private RecommendationService recommendationService;

    @MockBean
    private SpotifyClient spotifyClient;

    @MockBean
    private UserService userService;

    @MockBean
    private TrackRepository trackRepository;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private PlaylistRepository playlistRepository;

    private User testUser;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    @DisplayName("여러 스레드가 동시에 캐시 리프레시 시도할 때 락으로 인해 중복 리프레시가 발생하지 않음")
    void getRecommendations_with_concurrent_refresh_requests_should_not_cause_duplicatedRefresh() throws InterruptedException {
        int concurrentRequests = 50;
        int expectedApiCalls = 3; // 3번만 호출되어야 함

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);
        AtomicInteger apiCallCount = new AtomicInteger(0);

        testUser = new User(1L, "test@naver.com", Role.USER);
        given(userService.findByIdOrFail(anyLong())).willReturn(testUser);

        given(trackRepository.existsInUserPlaylist(any(), anyString())).willReturn(false);
        given(genreRepository.findTop5GenresByUser(any(), any())).willReturn(
                List.of(new Genre("pop"), new Genre("k-pop")));
        given(playlistRepository.findAllTrackByUser(any()))
                .willReturn(List.of(Track.builder().spotifyTrackId("test1").build(),
                        Track.builder().spotifyTrackId("test2").build()));

        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .willAnswer(invocation -> {
                    log.info("Spotify API 호출: {}", System.currentTimeMillis());
                    apiCallCount.incrementAndGet();

                    Thread.sleep(100); // API 지연

                    RecommendationResponse response = new RecommendationResponse();
                    List<TrackResponse> tracks = IntStream.range(0, 20)
                            .mapToObj(i -> builder()
                                    .id("spotifyId: "+i)
                                    .artists(new ArtistResponse("nct wish"))
                                    .build())
                            .collect(Collectors.toList());
                    response.setTracks(tracks);
                    return response;
                });

        //캐시 초기화
        recommendationService.getRecommendations(testUser.getId());

        List<Future<TrackRecommendationResponse>> futures = new ArrayList<>();
        for(int i = 0; i < concurrentRequests; i++) {
            futures.add(executorService.submit(() -> {
                startLatch.await();
                try {
                    return recommendationService.getRecommendations(testUser.getId());
                } finally {
                    completionLatch.countDown();
                }
            }));

            if (i % 5 == 0) {
                Thread.sleep(10);
            }
        }

        startLatch.countDown();
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
        log.info("기대한 API 호출 수: {}, 실제 발생한 API 호출 수: {}", expectedApiCalls, actualApiCalls);

        assertAll(
                () -> assertThat(results).hasSize(concurrentRequests), // 모든 요청 성공
                () -> assertThat(actualApiCalls)
                        .as("락 적용으로 API 호출이 최소화되어야 함. 기대값: %d, 실제: %d",
                                expectedApiCalls, actualApiCalls)
                        .isEqualTo(expectedApiCalls), // 정확히 3번만 호출되어야 함!!!!!!
                () -> verify(spotifyClient, times(expectedApiCalls))
                        .getRecommendations(anyString(), anyString(), anyInt())
        );

        executorService.shutdown();
    }

}