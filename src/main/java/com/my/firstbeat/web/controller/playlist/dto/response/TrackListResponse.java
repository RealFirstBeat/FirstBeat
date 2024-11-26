package com.my.firstbeat.web.controller.playlist.dto.response;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TrackListResponse {

    private List<TrackResponse> tracks;

    @NoArgsConstructor
    @Getter
    public static class TrackResponse{
        private Long id;
        private String trackName;
        private String artistName;
        private String albumCoverUrl;
        private String previewUrl;
    }

}
