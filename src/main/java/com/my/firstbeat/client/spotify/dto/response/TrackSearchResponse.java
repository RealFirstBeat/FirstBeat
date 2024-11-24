package com.my.spotify.client.spotify.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class SearchRespDto {

    private Integer limit;
    private String next;
    private String previous;
    private Integer total;
    private List<TestTrackRespDto> tracks;


    public SearchRespDto(Paging<Track> trackPage) {
        limit = trackPage.getLimit();
        next = trackPage.getNext();
        previous = trackPage.getPrevious();
        total = trackPage.getTotal();
        tracks = Arrays.stream(trackPage.getItems()).map(TestTrackRespDto::new).toList();
    }

    @NoArgsConstructor
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TestTrackRespDto{
        private String trackName;
        private String id;
        private Boolean isPlayable;
        private String previewUrl;
        private ArtistRespDto artists;
        private String uri;
        private String name;


        public TestTrackRespDto(Track track) {
            AlbumSimplified album = track.getAlbum();
            this.id = album.getId();
            this.isPlayable = track.getIsPlayable();
            this.previewUrl = track.getPreviewUrl();
            this.artists = new ArtistRespDto(album.getArtists());
            this.uri = track.getUri();
            this.name = album.getName();
            this.trackName = track.getName();
        }

        @NoArgsConstructor
        @Getter
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ArtistRespDto{
            private String name;

            public ArtistRespDto(ArtistSimplified[] artistSimplified) {
                this.name = artistSimplified[0].getName();
            }
        }
    }
}
