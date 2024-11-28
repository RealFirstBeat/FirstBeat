package com.my.firstbeat.web.config.async;

import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationRefreshTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class AsyncConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendationProperties properties;

    @Bean(name = "recommendationBackgroundExecutor")
    public ExecutorService recommendationBackgroundExecutor(AsyncProperties properties){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("recommendation-refresh-");
        executor.setCorePoolSize(properties.getRecommendation().getCorePoolSize());
        executor.setMaxPoolSize(properties.getRecommendation().getMaxPoolSize());
        executor.setQueueCapacity(properties.getRecommendation().getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getRecommendation().getKeepAliveSeconds());

        //거절 정책
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("작업 대기열이 가득 차서 작업이 거절되었습니다. 다음 작업에서 재시도합니다");
            if (r instanceof RecommendationRefreshTask task) {
                saveFailedTask(task.getUserId());
            }
        });

        //종료 정책
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(900); //15분

        executor.initialize();
        return executor.getThreadPoolExecutor();
    }

    private void saveFailedTask(Long userId){
        try{
            redisTemplate.opsForSet().add(
                    properties.getRedis().getFailedTasksKey(),
                    userId.toString()
            );
            redisTemplate.expire(
                    properties.getRedis().getFailedTasksKey(),
                    properties.getRedis().getFailedTasksTtlHours(),
                    TimeUnit.HOURS
            );
        } catch (RedisConnectionException e) {
            log.error("Redis 연결 실패로 갱신 작업 저장 실패 - userId: {}", userId, e);
        } catch (RedisException e) {
            log.error("Redis 작업 중 오류 발생 - userId: {}", userId, e);
        } catch (Exception e) {
            log.error("예상치 못한 오류로 갱신 작업 저장 실패 - userId: {}", userId, e);
        }
    }
}
