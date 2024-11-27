package com.my.firstbeat.web.service.recommemdation.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lock")
@Getter
@Setter
public class LockProperties {

    private String keyPrefix;
    private int waitTime;
    private int leaseTime;
    private int maxAttempts;
    private long initialBackOffMs;
    private double backOffMultiplier;
    private BackgroundLock background = new BackgroundLock();

    @Getter
    @Setter
    public static class BackgroundLock {
        private int maxAttempts = 2;
        private long initialBackOffMs = 500;
        private double backOffMultiplier = 2.0;
    }
}
