package com.my.firstbeat.client.spotify.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SpotifyClientMock {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Value("${spotify.mock.url}")
    private String mockServerUrl;


    public SpotifyClientMock(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        // HTTP 메시지 컨버터 추가
        List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());
        converters.add(new MappingJackson2HttpMessageConverter());
        restTemplate.setMessageConverters(converters);
    }

    public RecommendationResponse getRecommendations(String seedTracks, String seedGenres, int limit) {
        return RecommendationResponse.builder()
                .seeds(List.of(
                        RecommendationResponse.Seed.builder()
                                .afterFilteringSize(20)
                                .afterRelinkingSize(20)
                                .href("https://api.spotify.com/v1/artists/xx")
                                .id("seed_artist_id")
                                .initialPoolSize(100)
                                .type("ARTIST")
                                .build()
                ))
                .total(20)
                .trackResponses(Arrays.asList(
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Spicy")
                                .id("track_001")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/001")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("aespa").build())
                                .name("My World - The 3rd Mini Album")
                                .albumCoverUrl("https://mock.album.cover/001")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Better Things")
                                .id("track_002")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/002")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("aespa").build())
                                .name("Better Things - Single")
                                .albumCoverUrl("https://mock.album.cover/002")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Far Away")
                                .id("track_003")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/003")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT WISH").build())
                                .name("Far Away - NCT WISH")
                                .albumCoverUrl("https://mock.album.cover/002")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Red Flavor")
                                .id("track_004")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/004")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("Red Velvet").build())
                                .name("The Red Summer - Summer Mini Album")
                                .albumCoverUrl("https://mock.album.cover/004")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Favorite (Vampire)")
                                .id("track_005")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/005")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT 127").build())
                                .name("Favorite - The 3rd Album Repackage")
                                .albumCoverUrl("https://mock.album.cover/005")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Ring Ding Dong")
                                .id("track_006")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/006")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("SHINee").build())
                                .name("2009, Year Of Us")
                                .albumCoverUrl("https://mock.album.cover/006")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Electric Shock")
                                .id("track_007")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/007")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("f(x)").build())
                                .name("Electric Shock - The 2nd Mini Album")
                                .albumCoverUrl("https://mock.album.cover/007")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("ZOOM")
                                .id("track_008")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/008")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT DREAM").build())
                                .name("Beatbox - The 2nd Album Repackage")
                                .albumCoverUrl("https://mock.album.cover/008")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Feel My Rhythm")
                                .id("track_009")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/009")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("Red Velvet").build())
                                .name("The ReVe Festival 2022 - Feel My Rhythm")
                                .albumCoverUrl("https://mock.album.cover/009")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Sticker")
                                .id("track_010")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/010")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT 127").build())
                                .name("Sticker - The 3rd Album")
                                .albumCoverUrl("https://mock.album.cover/010")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Dreams Come True")
                                .id("track_011")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/011")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("aespa").build())
                                .name("Dreams Come True - Single")
                                .albumCoverUrl("https://mock.album.cover/011")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("View")
                                .id("track_012")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/012")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("SHINee").build())
                                .name("Odd - The 4th Album")
                                .albumCoverUrl("https://mock.album.cover/012")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Russian Roulette")
                                .id("track_013")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/013")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("Red Velvet").build())
                                .name("Russian Roulette - The 3rd Mini Album")
                                .albumCoverUrl("https://mock.album.cover/013")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Red Light")
                                .id("track_014")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/014")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("f(x)").build())
                                .name("Red Light")
                                .albumCoverUrl("https://mock.album.cover/014")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Regular")
                                .id("track_015")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/015")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT 127").build())
                                .name("Regular-Irregular - The 1st Album")
                                .albumCoverUrl("https://mock.album.cover/015")
                                .build(),

                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Candy")
                                .id("track_016")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/016")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT DREAM").build())
                                .name("Candy - Winter Special Mini Album")
                                .albumCoverUrl("https://mock.album.cover/016")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("WISH")
                                .id("track_017")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/017")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT WISH").build())
                                .name("WISH - NCT WISH")
                                .albumCoverUrl("https://mock.album.cover/016")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("P.O.V")
                                .id("track_018")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/018")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT WISH").build())
                                .name("P.O.V - NCT WISH")
                                .albumCoverUrl("https://mock.album.cover/016")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Song Bird")
                                .id("track_019")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/016")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("NCT WISH").build())
                                .name("Song Bird - NCT WISH")
                                .albumCoverUrl("https://mock.album.cover/016")
                                .build(),
                        RecommendationResponse.TrackResponse.builder()
                                .trackName("Don't Call Me")
                                .id("track_020")
                                .isPlayable(true)
                                .previewUrl("https://mock.preview.url/020")
                                .artists(RecommendationResponse.TrackResponse.Artists.builder().name("SHINee").build())
                                .name("Don't Call Me - The 7th Album")
                                .albumCoverUrl("https://mock.album.cover/020")
                                .build()
                ))
                .build();
    }

    @Builder
    @Getter
    public static class RecommendationResponse{
        private List<Seed> seeds;
        private int total;
        private List<TrackResponse> trackResponses;

        @Getter
        @Builder
        public static class Seed {
            private int afterFilteringSize;
            private int afterRelinkingSize;
            private String href;
            private String id;
            private int initialPoolSize;
            private String type;
        }

        @Getter
        @Builder
        public static class TrackResponse {
            private String trackName;
            private String id;
            private boolean isPlayable;
            private String previewUrl;
            private Artists artists;
            private String name;
            private String albumCoverUrl;

            @Getter
            @Builder
            public static class Artists {
                private String name;
            }
        }

    }

//    public RecommendationResponse getRecommendations(String seedTracks, String seedGenres, int limit) {
//
//
//
//
//
//
////        String url = mockServerUrl + "/spotify/recommendations";
////
////        log.info("Calling mock server URL: {}", url);
////        HttpHeaders headers = new HttpHeaders();
////        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
////        HttpEntity<String> entity = new HttpEntity<>(headers);
////
////
////        try {
////            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
////                    url,
////                    HttpMethod.GET,
////                    entity,
////                    RecommendationResponse.class
////            );
////
////            log.info("Mock server response status: {}", response.getStatusCode());
////            log.info("Mock server response body: {}", response.getBody());
////
////            if (response.getBody() == null) {
////                log.error("Received null response from mock server");
////            }
////
////            return response.getBody();
////        } catch (Exception e) {
////            log.error("Mock API 호출 실패: {}", e.getMessage(), e);
////            throw new SpotifyApiException(e);
////        }
//    }
}
