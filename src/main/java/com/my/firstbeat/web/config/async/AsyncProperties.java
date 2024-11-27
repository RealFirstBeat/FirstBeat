package com.my.firstbeat.web.config.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "async")
@Getter
public class AsyncProperties {
    private RecommendationExecutor recommendation = new RecommendationExecutor();

    @Getter
    public static class RecommendationExecutor {
        private int corePoolSize = 5;
        private int maxPoolSize = 10;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
    }
}
