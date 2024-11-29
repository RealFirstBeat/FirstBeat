package com.my.firstbeat.web.service.recommemdation;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.config.SpotifyClientMock;
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
import com.my.firstbeat.web.service.UserService;
import com.my.firstbeat.web.service.recommemdation.lock.RedisLockManager;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationRefreshTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final RedisLockManager lockManager;
    private final RecommendationProperties properties;
    private final ExecutorService recommendationBackgroundExecutor;
    private final Map<Long, AtomicInteger> concurrentRequestsPerUser = new ConcurrentHashMap<>(); //nGrinder 테스트 지표를 위해 추가
    private final SpotifyClientMock spotifyClientMock;

    // 메트릭 조회 메서드 추가 (nGrinder)
    public Map<Long, Integer> getConcurrentRequests() {
        Map<Long, Integer> metrics = new HashMap<>();
        concurrentRequestsPerUser.forEach((userId, count) ->
                metrics.put(userId, count.get()));
        return metrics;
    }

    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);
        String redisKey = toRedisKey(userId);

        //먼저 임계치 기반으로 트랙 갱신해야 하는지 확인
        refreshIfNeeded(user, redisKey);

        //락 획득 전 반환할게 있다면 빠른 반환 시도
        TrackRecommendationResponse quickTry = popRecommendation(redisKey);
        if(quickTry != null && !trackRepository.existsInUserPlaylist(user, quickTry.getSpotifyTrackId())){
            return quickTry;
        }

        // 빠르게 반환할 추천 트랙이 없고, 빠르게 반환될 수 있는 트랙이 사용자의 플레이리스트에 있다면
        // 락을 획득해서 반환 시작
        for(int attempts = 1; attempts <= properties.getMaxAttempts(); attempts++) {
            refreshIfNeeded(user, redisKey);
            TrackRecommendationResponse recommendation = popRecommendation(redisKey);
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

    private void refreshIfNeeded(User user, String redisKey) {
        if(needsRefresh(redisKey)) {
            lockManager.executeWithLockWithRetry(user.getId(), () -> {
                if(needsRefresh(redisKey)) {
                    refreshRecommendations(user, redisKey);
                }
                return Boolean.TRUE;
            }).orElseGet(() -> { //락 획득에 실패한 경우
                Long remainingSize = redisTemplate.opsForList().size(redisKey);
                log.warn("락 획득 실패로 인한 갱신 작업 실패. 유저: {}, 남은 추천 트랙 수: {}", user.getId(), remainingSize);

                //데이터가 있으면 반환
                if(remainingSize > 0) {
                    return Boolean.TRUE;
                }

                //반환할 수 있는 데이터가 없다면
                throw new BusinessException(ErrorCode.SERVICE_TEMPORARY_UNAVAILABLE);
            });
        }
    }

    private TrackRecommendationResponse popRecommendation(String redisKey) {
        return (TrackRecommendationResponse) redisTemplate.opsForList().leftPop(redisKey);
    }

    private void refreshRecommendations(User user, String redisKey) {
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
//                    spotifyClient.getRecommendations(seedTracks, seedGenres, properties.getSeedMax());


            SpotifyClientMock.RecommendationResponse spotifyRecommendations
                    = spotifyClientMock.getRecommendations(seedTracks, seedGenres, properties.getSeedMax());


            ListOperations<String, Object> listOps = redisTemplate.opsForList();

//            spotifyRecommendations.getTracks()
//                    .stream()
//                    .map(TrackRecommendationResponse::new)
//                    .forEach(track -> listOps.rightPush(redisKey, track));

            spotifyRecommendations.getTrackResponses()
                    .stream()
                    .map(TrackRecommendationResponse::new)
                    .forEach(track -> listOps.rightPush(redisKey, track));



            //24시간 후 만료
            redisTemplate.expire(redisKey, properties.getRedis().getCacheTtlHours(), TimeUnit.HOURS);
        } catch (SpotifyApiException e){
            log.error("추천 트랙 갱신 작업: Spotify API 호출 실패 - 유저 ID: {}, 원인: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("추천 트랙 갱신 작업: 예상치 못한 실패 - 유저 ID: {}, 원인: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    //백그라운드 캐시 갱신
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    @SchedulerLock(
            name = "backgroundRefresh",           // 락의 고유 이름
            lockAtLeastFor = "5m",               // 작업이 일찍 끝나도 최소한 5분간은 락 유지
            lockAtMostFor = "20m"                // 최대 20분 (15분 작업 + 여유 시간)
    )
    public void backgroundRefresh(){
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> refreshKeys = new ArrayList<>();

        //이전에 실패한 작업 먼저 가져오기
        Set<Object> failedTasks = redisTemplate.opsForSet()
                .members(properties.getRedis().getFailedTasksKey());

        if(!failedTasks.isEmpty()){
            log.info("이전 갱신 작업에서 실패한 {} 개의 작업을 먼저 처리합니다", failedTasks.size());
            failedTasks.stream()
                    .map(Object::toString)
                    .map(userId -> toRedisKey(Long.parseLong(userId)))
                    .forEach(refreshKeys::add);
        }

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(properties.getRedis().getKeyPrefix() + "*")
                .count(100)
                .build();
        Cursor<String> cursor = redisTemplate.scan(scanOptions);

        try{
            while(cursor.hasNext()){
                String key = cursor.next();
                if(needsRefresh(key)){
                    refreshKeys.add(key);
                }
            }
        } finally {
            cursor.close();
        }

        if (refreshKeys.isEmpty()) {
            log.info("갱신이 필요한 추천 트랙이 없습니다");
            return;
        }

        log.info("추천 트랙 갱신 백그라운드 리프레시 시작 - 대상 유저 수: {}", refreshKeys.size());

        // 갱신 작업 병렬 처리
        CountDownLatch latch = new CountDownLatch(refreshKeys.size());

        refreshKeys.forEach(key -> {
            Long userId = extractUserId(key);
            RecommendationRefreshTask task = new RecommendationRefreshTask(
                    userId,
                    key,
                    successCount,
                    failCount,
                    latch,
                    this
            );
            recommendationBackgroundExecutor.submit(task);
        });

        try{
            boolean completed = latch.await(15, TimeUnit.MINUTES);
            if(!completed){
                log.warn("일부 추천 트랙 갱신 작업이 제한 시간 내에 완료되지 않았습니다");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("추천 트랙 백그라운드 갱신 작업이 중단되었습니다: {}", e.getMessage(), e);
        }
        log.info("추천 트랙 갱신 백그라운드 리프레시 완료 - 성공: {}, 실패: {}", successCount.get(), failCount.get());
    }

    public void processRefresh(Long userId, String redisKey, AtomicInteger successCount, AtomicInteger failCount){
        try{
            User user;
            try {
                user = userService.findByIdOrFail(userId);
            } catch (BusinessException e) {
                failCount.getAndIncrement();
                log.warn("추천 트랙 백그라운드 갱신 작업: 유저 ID {}의 추천 트랙 갱신을 건너뜁니다. 사유: {}", userId, e.getMessage());
                return;
            }

            boolean isSuccess = lockManager.executeWithLockForBackground(userId, () -> {
                refreshRecommendations(user, redisKey);
            });
            if(isSuccess){
                successCount.getAndIncrement();
            } else {
                failCount.getAndIncrement();
                log.warn("추천 트랙 백그라운드 갱신 작업: 유저 ID {}의 추천 트랙 갱신 작업이 락 획득 실패로 건너뛰었습니다", userId);
            }
        } catch (SpotifyApiException e){
            failCount.getAndIncrement();
            log.error("추천 트랙 백그라운드 갱신 작업: Spotify API 호출 실패 - 유저 ID: {}, 원인: {}", userId, e.getMessage());
            log.debug("추천 트랙 백그라운드 갱신 작업: Spotify API 호출 실패 상세", e);
        } catch (Exception e){
            failCount.getAndIncrement();
            if(e instanceof BusinessException){
                log.warn("추천 트랙 백그라운드 갱신 작업: 유저 ID {}의 추천 트랙 갱신 중 비즈니스 예외 발생: {}", userId, e.getMessage());
                log.debug("추천 트랙 백그라운드 갱신 작업: 비즈니스 예외 발생. 상세 오류", e);
            } else{
                log.error("추천 트랙 백그라운드 갱신 작업: 유저 ID: {}의 추천 트랙 갱신 중 예기치 못한 오류 발생", userId, e);
                log.debug("추천 트랙 백그라운드 갱신 작업: 상세 오류", e);
            }
        }
    }

    public boolean needsRefresh(String redisKey) {
        Long size = redisTemplate.opsForList().size(redisKey);
        return size == null || size <= properties.getRefreshThreshold();
    }

    private Long extractUserId(String redisKey) {
        return Long.parseLong(redisKey.replace(properties.getRedis().getKeyPrefix(), ""));
    }

    private String toRedisKey(Long userId) {
        return properties.getRedis().getKeyPrefix() + userId;
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
}
