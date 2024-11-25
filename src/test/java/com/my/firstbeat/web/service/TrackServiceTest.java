package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.userGenre.UserGenreRepository;
import com.my.firstbeat.web.dummy.DummyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class TrackServiceTest extends DummyObject {

    @InjectMocks
    private TrackService trackService;

    @Mock
    private SpotifyClient spotifyClient;

    @Mock
    private UserService userService;

    @Mock
    private UserGenreRepository userGenreRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private TrackRepository trackRepository;

    private User testUser;
    private static final int CONCURRENT_USERS = 10;
    private static final int REQUESTS_PER_USER = 5;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@naver.com", Role.USER);

        given(userService.findByIdOrFail(testUser.getId())).willReturn(testUser);

        given(trackRepository.existsInUserPlaylist(any(), anyString())).willReturn(false);

        given(userGenreRepository.findTop5GenresByUser(any()))
                .willReturn(List.of(new Genre("pop"), new Genre("rock")));
        given(playlistRepository.findAllTrackByUser(any()))
                .willReturn(List.of(Track.builder().spotifyTrackId("test1").build(), Track.builder().spotifyTrackId("test2").build()));

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
    }

    @Test
    @DisplayName("여러 사용자가 동시에 추천 요청 테스트: 여러 스레드가 동시에 캐시에 접근할 때 안전한지 확인")
    void getRecommendations_with_multiple_threads_requests() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS); //10개
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS * REQUESTS_PER_USER); //10*5 = 50번
        List<Future<TrackRecommendationResponse>> futures = new ArrayList<>(); //비동기 결과 저장해야 함
        Set<String> recommendedTracks = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger requestCount = new AtomicInteger(0);

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


                        TrackRecommendationResponse response = trackService.getRecommendations(testUser.getId());
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
    @DisplayName("여러 스레드가 동시에 캐시 리프레시 시도할 때 중복 리프레시 발생하지 않는지 테스트")
    void getRecommendations_with_concurrent_refresh_requests_shouldNot_duplicatedRefresh(){

        int concurrentRequests = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests); //10개
        CountDownLatch latch = new CountDownLatch(1); //10*5 = 50번
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        given(spotifyClient.getRecommendations(anyString(), anyString(), anyInt()))
                .willAnswer(invocation -> {
                    Thread.sleep(100);
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

        List<Future<TrackRecommendationResponse>> futures = new ArrayList<>();
        for(int i = 0;i<concurrentRequests; i++){

        }

    }





}