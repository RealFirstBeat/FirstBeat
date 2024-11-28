package com.my.firstbeat.web.config.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "async")
@Getter
@Setter
public class AsyncProperties {
    private RecommendationExecutor recommendation = new RecommendationExecutor();

    @Getter
    @Setter
    public static class RecommendationExecutor {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int keepAliveSeconds;
    }
}
