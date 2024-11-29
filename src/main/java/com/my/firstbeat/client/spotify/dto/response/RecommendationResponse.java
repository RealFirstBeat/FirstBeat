package com.my.firstbeat.client.spotify.dto.response;

import lombok.*;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.RecommendationsSeed;

import java.util.Arrays;
import java.util.List;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;


@NoArgsConstructor
@Getter
@Setter
public class RecommendationResponse {

    private RecommendationsSeed[] seeds;
    private Integer total;
    private List<TrackResponse> tracks;

    public RecommendationResponse(Recommendations recommendations) {
        this.seeds = recommendations.getSeeds();
        this.tracks = Arrays.stream(recommendations.getTracks()).map(TrackResponse::new).toList();
        this.total = tracks.size();
    }
}
