package com.my.firstbeat.web.service.recommemdation.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lock")
@Getter
public class LockProperties {

    private String keyPrefix;
    private int waitTime;
    private int leaseTime;
}
