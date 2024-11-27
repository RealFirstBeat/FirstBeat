package com.my.firstbeat.web.service.recommemdation.property;

import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithRedis;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
public class RecommendationRefreshTask implements Runnable {
    private final Long userId;
    private final String redisKey;
    private final AtomicInteger successCount;
    private final AtomicInteger failCount;
    private final CountDownLatch latch;
    private final RecommendationServiceWithRedis service;

    @Override
    public void run() {
        try {
            service.processRefresh(userId, redisKey, successCount, failCount);
        } finally {
            latch.countDown();
        }
    }
}
