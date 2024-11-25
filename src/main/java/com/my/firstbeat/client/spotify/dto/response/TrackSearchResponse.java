package com.my.firstbeat.client.spotify.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.List;


@NoArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrackSearchResponse {

    private Integer limit;
    private String next;
    private String previous;
    private Integer total;
    private List<TrackResponse> tracks;


    public TrackSearchResponse(Paging<Track> trackPage) {
        limit = trackPage.getLimit();
        next = trackPage.getNext();
        previous = trackPage.getPrevious();
        total = trackPage.getTotal();
        tracks = Arrays.stream(trackPage.getItems()).map(TrackResponse::new).toList();
    }

    @NoArgsConstructor
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @AllArgsConstructor
    @Builder
    public static class TrackResponse {
        private String trackName;
        private String id;
        private Boolean isPlayable;
        private String previewUrl;
        private ArtistResponse artists;
        private String name;
        private String albumCoverUrl;


        public TrackResponse(Track track) {
            AlbumSimplified album = track.getAlbum();
            this.id = album.getId();
            this.isPlayable = track.getIsPlayable();
            this.previewUrl = track.getPreviewUrl();
            this.artists = new ArtistResponse(album.getArtists());
            this.name = album.getName();
            this.trackName = track.getName();
            this.albumCoverUrl = Arrays.stream(album.getImages()).toList().get(0).getUrl();
        }

        @NoArgsConstructor
        @Getter
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        @AllArgsConstructor
        public static class ArtistResponse {
            private String name;

            public ArtistResponse(ArtistSimplified[] artistSimplified) {
                this.name = artistSimplified[0].getName();
            }
        }
    }
}
