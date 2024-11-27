package com.my.firstbeat.web.service.recommemdation.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation")
@Getter
public class RecommendationProperties {
    private int refreshThreshold = 5;
    private int size = 20;
    private int maxAttempts = 20;
    private int seedMax = 5;
    private Redis redis = new Redis();

    @Getter
    public static class Redis {
        private String keyPrefix = "recommendation:user:";
        private long cacheTtlHours = 24;
    }
}