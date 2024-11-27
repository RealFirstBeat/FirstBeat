package com.my.firstbeat.web.service;

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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
public class RecommendationServiceWithRedis {

    private final SpotifyClient spotifyClient;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>(); // 유저별 락

    private static final String RECOMMENDATION_KEY_PREFIX = "recommendation:user:";
    private static final int REFRESH_THRESHOLD = 5;
    private static final int RECOMMENDATIONS_SIZE = 20;
    private static final int MAX_ATTEMPTS = 20;
    private static final int SEED_MAX = 5;

    private static final long CACHE_TTL_HOURS = 24;

    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);
        String redisKey = RECOMMENDATION_KEY_PREFIX + userId;


        //락 획득 전 반환할게 있다면 빠른 반환 시도
        TrackRecommendationResponse quickTry = popRecommendation(redisKey);
        if(quickTry != null && !trackRepository.existsInUserPlaylist(user, quickTry.getSpotifyTrackId())){
            return quickTry;
        }

        // 빠르게 반환할 추천 트랙이 없고, 빠르게 반환될 수 있는 트랙이 사용자의 플레이리스트에 있다면
        // 락을 획득해서 반환 시작
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, key -> new ReentrantLock());

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++) {
            userLock.lock();
            try {
                if(needsRefresh(redisKey)) {
                    refreshRecommendations(user, redisKey);
                }
            } finally {
                userLock.unlock();
            }
            TrackRecommendationResponse recommendation = popRecommendation(redisKey);
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

    private TrackRecommendationResponse popRecommendation(String redisKey) {
        return (TrackRecommendationResponse) redisTemplate.opsForList().leftPop(redisKey);
    }

    private void refreshRecommendations(User user, String redisKey) {
        try {
            String seedGenres = getSeedGenres(user);
            String seedTracks = getSeedTracks(user);

            RecommendationResponse spotifyRecommendations =
                    spotifyClient.getRecommendations(seedTracks, seedGenres, RECOMMENDATIONS_SIZE);

            ListOperations<String, Object> listOps = redisTemplate.opsForList();

            spotifyRecommendations.getTracks()
                    .stream()
                    .map(TrackRecommendationResponse::new)
                    .forEach(track -> listOps.rightPush(redisKey, track));

            //24시간 후 만료
            redisTemplate.expire(redisKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
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

        Set<String> keys = redisTemplate.keys(RECOMMENDATION_KEY_PREFIX + "*");
        if(keys.isEmpty()){
            return;
        }

        List<String> refreshKeys = keys.stream()
                .filter(this::needsRefresh)
                .toList();

        log.info("추천 트랙 갱신 백그라운드 리프레시 시작 - 대상 유저 수: {}", refreshKeys.size());

        refreshKeys
                .forEach(key -> {
                    Long userId = Long.parseLong(key.replace(RECOMMENDATION_KEY_PREFIX, ""));
                    ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
                    boolean locked = false;

                    try{
                        locked = userLock.tryLock(1000, TimeUnit.MILLISECONDS);
                        if(locked){
                            User user = userService.findByIdOrFail(userId);
                            refreshRecommendations(user, key);
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
        Set<String> keys = redisTemplate.keys(RECOMMENDATION_KEY_PREFIX + "*");
        userLocks.keySet().removeIf(userId -> !keys.contains(RECOMMENDATION_KEY_PREFIX + userId));
    }

    public boolean needsRefresh(String redisKey) {
        Long size = redisTemplate.opsForList().size(redisKey);
        return size == null || size <= REFRESH_THRESHOLD;
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
}
