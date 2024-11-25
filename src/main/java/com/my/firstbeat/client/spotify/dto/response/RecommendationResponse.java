package com.my.spotify.client.spotify.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.RecommendationsSeed;

import java.util.Arrays;
import java.util.List;

import static com.my.spotify.client.spotify.dto.SearchRespDto.*;

@NoArgsConstructor
@Getter
public class RecommendationRespDto {

    private RecommendationsSeed[] seeds;
    private Integer total;
    private List<TestTrackRespDto> tracks;

    public RecommendationRespDto(Recommendations recommendations) {
        this.seeds = recommendations.getSeeds();
        this.tracks = Arrays.stream(recommendations.getTracks()).map(TestTrackRespDto::new).toList();
        this.total = tracks.size();
    }
}
