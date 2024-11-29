package com.my.firstbeat.web.service.recommemdation;

import com.github.benmanes.caffeine.cache.Cache;
import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.config.SpotifyClientMock;
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
import com.my.firstbeat.web.service.UserService;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
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
    private final RecommendationProperties properties;

    private final SpotifyClientMock spotifyClientMock;

    private final Map<Long, AtomicInteger> concurrentRequestsPerUser = new ConcurrentHashMap<>(); //nGrinder 테스트 지표를 위해 추가

    // 메트릭 조회 메서드 추가 (nGrinder)
    public Map<Long, Integer> getConcurrentRequests() {
        Map<Long, Integer> metrics = new HashMap<>();
        concurrentRequestsPerUser.forEach((userId, count) ->
                metrics.put(userId, count.get()));
        return metrics;
    }

    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);
        Queue<TrackRecommendationResponse> recommendations =
                recommendationsCache.get(userId, key -> new ConcurrentLinkedQueue<>());

        //먼저 갱신이 필요한지 체크 -> 임계치 기반 갱신 해야 함
        if(needsRefresh(recommendations)) {
            //락 획득 후 갱신
            ReentrantLock userLock = userLocks.computeIfAbsent(userId, key -> new ReentrantLock());
            userLock.lock();
            try {
                if(needsRefresh(recommendations)) {
                    refreshRecommendations(user);
                    recommendations = recommendationsCache.get(userId, key -> new ConcurrentLinkedQueue<>());
                }
            } finally {
                userLock.unlock();
            }
        }

        //추천 트랙 반환
        for(int attempts = 1; attempts <= properties.getMaxAttempts(); attempts++) {
            TrackRecommendationResponse recommendation = recommendations.poll();
            if(recommendation == null) {
                throw new BusinessException(ErrorCode.FAIL_TO_GET_RECOMMENDATION);
            }
            if(!trackRepository.existsInUserPlaylist(user, recommendation.getSpotifyTrackId())) {
                return recommendation;
            }
            log.debug("유저: {}의 플레이리스트에 추천 트랙: {} 존재. 추천 재시도: {}",
                    user.getId(), recommendation.getSpotifyTrackId(), attempts);
        }

        throw new BusinessException(ErrorCode.MAX_RECOMMENDATION_ATTEMPTS_EXCEED);
    }

    private void refreshRecommendations(User user) {

        AtomicInteger concurrent = concurrentRequestsPerUser.computeIfAbsent(user.getId(), k -> new AtomicInteger(0)); //nGrinder
        int concurrentCount = concurrent.incrementAndGet(); //nGrinder


        try {

            if (concurrentCount > 1) {
                log.warn("중복 API calls 호출 발생 유저 ID: {}, 동시 요청 카운트: {}",
                        user.getId(), concurrentCount);
            }

            String seedGenres = getSeedGenres(user);
            String seedTracks = getSeedTracks(user);

//            RecommendationResponse spotifyRecommendations =
//                    spotifyClient.getRecommendations(seedTracks, seedGenres, properties.getSize());

            log.info("리프레쉬 레코멘데이션 들어옴... 유저 ID: {}", user.getId());
//            RecommendationResponse spotifyRecommendations =
//                    spotifyClientMock.getRecommendations(seedTracks, seedGenres, properties.getSize());

            SpotifyClientMock.RecommendationResponse spotifyRecommendations
                    = spotifyClientMock.getRecommendations(seedTracks, seedGenres, properties.getSize());

            Queue<TrackRecommendationResponse> recommendations = recommendationsCache.get(user.getId(), key -> new ConcurrentLinkedQueue<>());
            spotifyRecommendations.getTrackResponses()
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
    public void cleanupUserLocks(){
        userLocks.keySet().removeIf(userId -> !recommendationsCache.asMap().containsKey(userId));
    }

    public boolean needsRefresh(Queue<TrackRecommendationResponse> recommendations) {
        return recommendations.size() <= properties.getRefreshThreshold();
    }


    private String getSeedGenres(User user){
        String seedGenres = genreRepository.findRandomGenresByUser(user.getId(), properties.getSeedMax())
                .stream()
                .map(Genre::getName)
                .collect(Collectors.joining(","));
        if(seedGenres.isEmpty()){
            throw new BusinessException(ErrorCode.GENRES_NOT_FOUND);
        }
        return seedGenres;
    }


    private String getSeedTracks(User user){
        List<Track> trackList = playlistRepository.findRandomTrackByUser(user, PageRequest.of(0, properties.getSeedMax()));
        return trackList.stream()
                .limit(properties.getSeedMax())
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
