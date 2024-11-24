package com.my.firstbeat.client.spotify.config.env;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Component
@Slf4j
public class EnvLoader {

    @PostConstruct
    public void loadEnv() throws IOException {
        Properties props = new Properties();
        ClassPathResource resource = new ClassPathResource(".env");
        try (FileInputStream fileInputStream = new FileInputStream(resource.getFile())) {
            props.load(fileInputStream);
        }
        props.forEach((key, value) -> System.setProperty((String) key, (String) value));
        log.debug("디버그: spotify 환경변수 설정 완료");
    }
}