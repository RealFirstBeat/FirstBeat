package com.my.firstbeat.web.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
//락 적용
public class RecommendationService {

    private final SpotifyClient spotifyClient;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    private final Cache<Long, Queue<TrackRecommendationResponse>> recommendationsCache;
    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>(); //유저 별 락
    private static final int REFRESH_THRESHOLD = 5; // 5개 남으면 새로 다시 요청
    private static final int RECOMMENDATIONS_SIZE = 20; //한 번에 받아오는 추천 트랙 수
    private static final int MAX_ATTEMPTS = 20; //추천한 곡이 이미 유저의 플레이리스트에 있는 경우 다시 추천 큐에서 꺼내올 수 있는 최대 횟수
    private static final int SEED_MAX = 5;



    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);
        Queue<TrackRecommendationResponse> recommendations =
                recommendationsCache.get(userId, key -> new ConcurrentLinkedQueue<>());

        //락 획득 전 반환할게 있다면 빠른 반환 시도
        TrackRecommendationResponse quickTry = recommendations.poll();
        if(quickTry != null && !trackRepository.existsInUserPlaylist(user, quickTry.getSpotifyTrackId())){
            return quickTry;
        }

        // 빠르게 반환할 추천 트랙이 없고, 빠르게 반환될 수 있는 트랙이 사용자의 플레이리스트에 있다면
        // 락을 획득해서 반환 시작
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, key -> new ReentrantLock());

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++) {
            userLock.lock();
            try {
                if(needsRefresh(recommendations)) {
                    refreshRecommendations(user);
                    //갱신 후 캐시에서 추천 리스트 다시 가져옴
                    recommendations = recommendationsCache.get(userId, key -> new ConcurrentLinkedQueue<>());
                }
            } finally {
                userLock.unlock();
            }
            TrackRecommendationResponse recommendation = recommendations.poll();
            if(recommendation == null) {
                throw new BusinessException(ErrorCode.FAIL_TO_GET_RECOMMENDATION);
            }
            if(!trackRepository.existsInUserPlaylist(user, recommendation.getSpotifyTrackId())) {
                return recommendation;
            }
            log.debug("유저: {}의 플레이리스트에 추천 트랙: {} 존재. 추천 재시도: {}", user.getId(), recommendation.getSpotifyTrackId(), attempts);
        }

        throw new BusinessException(ErrorCode.MAX_RECOMMENDATION_ATTEMPTS_EXCEED);
    }

    private void refreshRecommendations(User user) {
        try {
            String seedGenres = getSeedGenres(user);
            String seedTracks = getSeedTracks(user);

            RecommendationResponse spotifyRecommendations =
                    spotifyClient.getRecommendations(seedTracks, seedGenres, RECOMMENDATIONS_SIZE);

            Queue<TrackRecommendationResponse> recommendations = recommendationsCache.get(user.getId(), key -> new ConcurrentLinkedQueue<>());
            spotifyRecommendations.getTracks()
                    .stream()
                    .map(TrackRecommendationResponse::new)
                    .forEach(recommendations::offer);
            // 데이터 갱신
            recommendationsCache.put(user.getId(), recommendations);
        } catch (SpotifyApiException e){
            log.error("Spotify API 호출 실패 - 유저 ID: {}, 원인: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("예상치 못한 추천 트랙 갱신 실패 - 유저 ID: {}, 원인: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    //백그라운드 캐시 갱신
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void backgroundRefresh(){
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        var refreshEntries = recommendationsCache.asMap()
                .entrySet()
                .stream()
                .filter(entry -> needsRefresh(entry.getValue()))
                .toList();
        log.info("추천 트랙 갱신 백그라운드 리프레시 시작 - 대상 유저 수: {}", refreshEntries.size());

        refreshEntries
                .forEach(entry -> {
                    Long userId = entry.getKey();
                    ReentrantLock userLock = userLocks.computeIfAbsent(userId, key -> new ReentrantLock());
                    boolean locked = false;

                    try{
                        locked = userLock.tryLock(1000, TimeUnit.MILLISECONDS);
                        if(locked){
                            User user = userService.findByIdOrFail(userId);
                            refreshRecommendations(user);
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e){
                        failCount.incrementAndGet();
                        log.error("백그라운드 추천 트랙 갱신 작업 실패 - 유저 ID: {}, 원인: {}", userId, e.getMessage(), e);
                    }finally {
                        if(locked){
                            userLock.unlock();
                        }
                    }
                });

        log.info("추천 트랙 갱신 백그라운드 리프레시 완료 - 성공: {}, 실패: {}", successCount.get(), failCount.get());
    }

    //userLock 클린업
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    private void cleanupUserLocks(){
        userLocks.keySet().removeIf(userId -> !recommendationsCache.asMap().containsKey(userId));
    }

    public boolean needsRefresh(Queue<TrackRecommendationResponse> recommendations) {
        return recommendations.size() <= REFRESH_THRESHOLD;
    }


    private String getSeedGenres(User user){
        String seedGenres = genreRepository.findTop5GenresByUser(user, PageRequest.of(0, SEED_MAX))
                .stream()
                .map(Genre::getName)
                .collect(Collectors.joining(","));
        if(seedGenres.isEmpty()){
            throw new BusinessException(ErrorCode.GENRES_NOT_FOUND);
        }
        return seedGenres;
    }


    private String getSeedTracks(User user){
        List<Track> trackList = playlistRepository.findAllTrackByUser(user, PageRequest.of(0, SEED_MAX));
        return trackList.stream()
                .limit(SEED_MAX)
                .map(Track::getSpotifyTrackId)
                .collect(Collectors.joining(","));
    }

	public boolean removeTrackFromRecommendations(Long userId, String spotifyTrackId) {
        Queue<TrackRecommendationResponse> recommendations =
            recommendationsCache.get(userId, key -> new ConcurrentLinkedQueue<>());

        // 큐에서 해당 트랙 제거
        return recommendations.removeIf(
            recommendation -> recommendation.getSpotifyTrackId().equals(spotifyTrackId)
        );
    }
}
