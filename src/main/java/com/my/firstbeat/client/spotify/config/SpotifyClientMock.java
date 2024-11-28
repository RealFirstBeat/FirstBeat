package com.my.firstbeat.client.spotify.config;

import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyClientMock {

    private final RestTemplate restTemplate;

    @Value("${spotify.mock.url}")
    private String mockServerUrl;

    public RecommendationResponse getRecommendations(String seedTracks, String seedGenres, int limit) {
        String url = mockServerUrl + "/spotify/recommendations";

        try {
            ResponseEntity<Recommendations> response =
                    restTemplate.getForEntity(url, Recommendations.class);

            return new RecommendationResponse(response.getBody());
        } catch (Exception e) {
            log.error("Mock API 호출 실패: {}", e.getMessage(), e);
            throw new SpotifyApiException(e);
        }
    }
}
