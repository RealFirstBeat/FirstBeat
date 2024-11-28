package com.my.firstbeat.web.config;

import com.my.firstbeat.web.config.async.AsyncProperties;
import com.my.firstbeat.web.service.recommemdation.property.LockProperties;
import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {RecommendationProperties.class, LockProperties.class, AsyncProperties.class})
public class RecommendationConfig {
}
