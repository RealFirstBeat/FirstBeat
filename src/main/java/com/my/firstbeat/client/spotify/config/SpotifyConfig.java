package com.my.firstbeat.client.spotify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class SpotifyConfig {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.url}")
    private String redirectUrl;

    @Bean
    public SpotifyApi spotifyApi() throws URISyntaxException {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(new URI(redirectUrl))
                .build();
    }
}
