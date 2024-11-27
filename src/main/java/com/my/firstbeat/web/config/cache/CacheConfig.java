package com.my.firstbeat.web.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public Cache<Long, Queue<TrackRecommendationResponse>> recommendationsCache()  {
        return Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .expireAfterAccess(6, TimeUnit.HOURS)
                .maximumSize(10000)
                .recordStats()
                .removalListener((key, value, cause) ->
                        log.info("캐시 삭제 - Key: {}, Cause: {}", key, cause))
                .build();
    }

}
