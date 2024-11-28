package com.my.firstbeat.web.controller.playlist.dto.response;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.domain.track.Track;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@NoArgsConstructor
@Getter
public class TrackListResponse {

    private List<TrackResponse> tracks;
    private long totalElements;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;

    public TrackListResponse(Page<Track> trackPage) {
        this.tracks = trackPage.getContent().stream().map(TrackResponse::new).toList();
        this.totalElements = trackPage.getTotalElements();
        this.totalPages = trackPage.getTotalPages();
        this.hasPrevious = trackPage.hasPrevious();
        this.hasNext = trackPage.hasNext();
    }

    @NoArgsConstructor
    @Getter
    public static class TrackResponse{
        public TrackResponse(Track track) {
            this.id = track.getId();
            this.trackName = track.getName();
            this.artistName = track.getArtistName();
            this.albumCoverUrl = track.getPreviewUrl();
            this.previewUrl = track.getPreviewUrl();
        }

        private Long id;
        private String trackName;
        private String artistName;
        private String albumCoverUrl;
        private String previewUrl;
    }

}
