package com.my.firstbeat.web.controller;

import com.my.firstbeat.web.service.recommemdation.RecommendationService;
import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithRedis;
import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithoutLock;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricController {
    private final RecommendationServiceWithoutLock recommendationServiceWithoutLock;
    private final RecommendationService recommendationService;
    private final RecommendationServiceWithRedis recommendationServiceWithRedis;

    @GetMapping("/v1/metrics/concurrent-calls")
    public Map<Long, Integer> getConcurrentCallsV1() {
        return recommendationServiceWithoutLock.getConcurrentRequests();
    }

    @GetMapping("/v2/metrics/concurrent-calls")
    public Map<Long, Integer> getConcurrentCallsV2() {
        return recommendationService.getConcurrentRequests();
    }

    @GetMapping("/v3/metrics/concurrent-calls")
    public Map<Long, Integer> getConcurrentCallsV3() {
        return recommendationServiceWithRedis.getConcurrentRequests();
    }

}