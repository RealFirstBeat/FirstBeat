package com.my.firstbeat.web.service.recommemdation.lock;

import com.my.firstbeat.web.service.recommemdation.lock.metric.LockExecutionMetrics;
import com.my.firstbeat.web.service.recommemdation.lock.metric.LockMetricsConstants;
import com.my.firstbeat.web.service.recommemdation.property.LockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockManager {

    private final RedissonClient redissonClient;

    private final LockProperties properties;
    private final LockExecutionMetrics metrics;
    private static final String BACKGROUND_OPERATION = "백그라운드 추천 트랙 갱신 작업";
    private static final String OPERATION = "추천 트랙 갱신 작업";

    //애플리케이션 레벨에서의 재시도 메커니즘 적용한 락 획득 후 작업 실행
    private <T> Optional<T> executeWithLock(
            Long key,
            Supplier<T> action,
            int maxAttempts,
            long initialBackoffMs,
            double backoffMultiplier,
            boolean isBackground) {

        int attempts = 0;
        long backoffMs = initialBackoffMs;
        String operationType = isBackground ? BACKGROUND_OPERATION : OPERATION;

        while (attempts < maxAttempts) {
            RLock lock = redissonClient.getLock(generateLockKey(key));
            long startTime = System.currentTimeMillis();

            try {
                boolean isLocked = lock.tryLock(properties.getWaitTime(), properties.getLeaseTime(), TimeUnit.SECONDS);
                long duration = System.currentTimeMillis() - startTime;

                if (isLocked) {
                    metrics.recordLockAttempt(key, true, duration, attempts, isBackground);
                    metrics.recordLockDuration(key, duration, isBackground);

                    try {
                        T result = action.get();
                        return Optional.ofNullable(result);
                    } catch (Exception e) {
                        metrics.recordLockError(key,
                                isBackground ? LockMetricsConstants.ERROR_BACKGROUND_UNEXPECTED : LockMetricsConstants.ERROR_UNEXPECTED,
                                isBackground);
                        throw e;
                    }
                }

                attempts++;
                metrics.recordLockAttempt(key, false, duration, attempts, isBackground);

                if (attempts < maxAttempts) {
                    log.warn("key: {}에 대한 {} 락 획득 실패. 시도 횟수: {}/{}. {}ms 후 재시도",
                            key, operationType, attempts, maxAttempts, backoffMs);
                    Thread.sleep(backoffMs);
                    backoffMs *= backoffMultiplier;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                metrics.recordLockError(key,
                        isBackground ? LockMetricsConstants.ERROR_BACKGROUND_INTERRUPTED : LockMetricsConstants.ERROR_INTERRUPTED,
                        isBackground);
                log.error("key: {}에 대한 {} 락 처리 중 인터럽트 발생. 시도 횟수: {}/{}",
                        key, operationType, attempts, maxAttempts, e);
                return Optional.empty();
            } catch (Exception e) {
                metrics.recordLockError(key,
                        isBackground ? LockMetricsConstants.ERROR_BACKGROUND_UNEXPECTED : LockMetricsConstants.ERROR_UNEXPECTED,
                        isBackground);
                log.error("key: {}에 대한 {} 락 처리 중 예기치 못한 오류 발생. 시도 횟수: {}/{}",
                        key, operationType, attempts, maxAttempts, e);
                throw e;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("{} 락 작업 완료. 소요 시간: {}ms, 키: {}, 시도 횟수: {}/{}",
                            operationType, duration, key, attempts + 1, maxAttempts);
                    lock.unlock();
                }
            }
        }

        metrics.recordLockTimeout(key, isBackground);
        log.error("key: {}에 대한 {} 락 획득 실패. 최대 시도 횟수({}) 도달", key, operationType, maxAttempts);
        return Optional.empty();
    }

    public <T> Optional<T> executeWithLockWithRetry(Long key, Supplier<T> action) {
        return executeWithLock(
                key,
                action,
                properties.getMaxAttempts(),
                properties.getInitialBackOffMs(),
                properties.getBackOffMultiplier(),
                false
        );
    }

    public boolean executeWithLockForBackground(Long key, Runnable action) {
        return executeWithLock(
                key,
                () -> { action.run(); return true; },
                properties.getBackground().getMaxAttempts(),
                properties.getBackground().getInitialBackOffMs(),
                properties.getBackground().getBackOffMultiplier(),
                true
        ).orElse(false); //작업에 실패해 Optional.empty()가 반환되면 작업 실패로 설정
    }

    private String generateLockKey(Long key) {
        return properties.getKeyPrefix() + key;
    }


}
