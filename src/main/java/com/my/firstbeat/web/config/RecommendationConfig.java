package com.my.firstbeat.web.config;

import com.my.firstbeat.web.service.recommemdation.property.RecommendationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RecommendationProperties.class)
public class RecommendationConfig {
}
