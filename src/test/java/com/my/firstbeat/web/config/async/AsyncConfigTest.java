package com.my.firstbeat.web.config.async;

import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithRedis;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationRefreshTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.client.RedisConnectionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncConfigTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private RecommendationProperties properties;

    @Mock
    private AsyncProperties asyncProperties;

    @Mock
    private AsyncProperties.RecommendationExecutor recommendationExecutor;

    @Mock
    private RecommendationServiceWithRedis recommendationService;

    private AsyncConfig asyncConfig;

    private static final String REDIS_KEY = "recommendation:user:1";


    @BeforeEach
    void setUp() {

        properties = new RecommendationProperties();
        properties.setMaxAttempts(3);
        properties.setRefreshThreshold(5);

        Redis redis = new Redis();
        redis.setFailedTasksKey("recommendation:failed-refresh");
        redis.setCacheTtlHours(24);
        redis.setKeyPrefix("recommendation:user:");

        properties.setRedis(redis);

        asyncConfig = new AsyncConfig(redisTemplate, properties);

        when(asyncProperties.getRecommendation()).thenReturn(recommendationExecutor);

        when(recommendationExecutor.getCorePoolSize()).thenReturn(5);
        when(recommendationExecutor.getMaxPoolSize()).thenReturn(10);
        when(recommendationExecutor.getQueueCapacity()).thenReturn(100);
        when(recommendationExecutor.getKeepAliveSeconds()).thenReturn(60);
    }

    @Test
    @DisplayName("Executor 기본 설정이 올바르게 적용되는지 검증")
    void validateExecutorBasicConfiguration() {
        ExecutorService executor = asyncConfig.recommendationBackgroundExecutor(asyncProperties);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

        assertAll(
                () -> assertEquals(5, threadPoolExecutor.getCorePoolSize()),
                () -> assertEquals(10, threadPoolExecutor.getMaximumPoolSize()),
                () -> assertEquals(100, threadPoolExecutor.getQueue().remainingCapacity()),
                () -> assertTrue(threadPoolExecutor.getThreadFactory().newThread(()-> {})
                        .getName().startsWith("recommendation-refresh-"))
        );
    }

    @Test
    @DisplayName("작업 거절시 Redis에 실패 작업이 저장되는지 검증")
    void validateRejectedTaskSavedToRedis() {

        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        ExecutorService executor = asyncConfig.recommendationBackgroundExecutor(asyncProperties);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        Long userId = 1L;

        RecommendationRefreshTask task = new RecommendationRefreshTask(
                userId,
                REDIS_KEY,
                new AtomicInteger(0),
                new AtomicInteger(0),
                new CountDownLatch(1),
                recommendationService
        );
        threadPoolExecutor.getRejectedExecutionHandler()
                .rejectedExecution(task, threadPoolExecutor);


        verify(setOperations).add(properties.getRedis().getFailedTasksKey(), userId.toString());
        verify(redisTemplate).expire(
                eq(properties.getRedis().getFailedTasksKey()),
                eq(24L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("Redis 저장 실패시 예외가 적절히 처리되는지 검증")
    void validateRedisFailureHandling() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add(anyString(), anyString()))
                .thenThrow(new RedisConnectionException("연결 오류 발생"));

        ExecutorService executor = asyncConfig.recommendationBackgroundExecutor(asyncProperties);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        Long userId = 1L;

        RecommendationRefreshTask task = new RecommendationRefreshTask(
                userId,
                REDIS_KEY,
                new AtomicInteger(0),
                new AtomicInteger(0),
                new CountDownLatch(1),
                recommendationService
        );

        assertDoesNotThrow(() ->
                threadPoolExecutor.getRejectedExecutionHandler()
                        .rejectedExecution(task, threadPoolExecutor)
        );
    }


    @Test
    @DisplayName("종료 시 진행 중인 작업이 완료될 때까지 대기하는지 검증")
    void validateGracefulShutdown() throws InterruptedException {
        ExecutorService executor = asyncConfig.recommendationBackgroundExecutor(asyncProperties);
        CountDownLatch taskLatch = new CountDownLatch(1);
        AtomicBoolean taskCompleted = new AtomicBoolean(false);

        executor.submit(() -> {
            try {
                Thread.sleep(500); // 작업 실행중
                taskCompleted.set(true); //작업 완료
                taskLatch.countDown(); //래치 카운트
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        boolean terminatedInTime = executor.awaitTermination(2, TimeUnit.SECONDS); //일단 테스트에서는 2초 안에 정상 종료되는지 검증

        assertTrue(terminatedInTime, "Executor가 시간 내에 종료되어야 합니다");
        assertTrue(taskLatch.await(1, TimeUnit.SECONDS), "작업이 완료되어야 합니다");
        assertTrue(taskCompleted.get(), "작업이 정상적으로 완료되어야 합니다");
    }


}