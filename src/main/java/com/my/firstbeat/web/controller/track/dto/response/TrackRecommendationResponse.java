package com.my.firstbeat.web.controller.track.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;

@NoArgsConstructor
@Getter
public class TrackRecommendationResponse {

    private String spotifyTrackId; //스포티파이 트랙 아이디
    private String name; //곡 제목
    private String previewUrl; //프리뷰 url
    private String albumCoverUrl; //앨범 커버 url
    private String artistName; //가수 이름


    public TrackRecommendationResponse(TrackResponse trackResponse) {
        this.spotifyTrackId = trackResponse.getId();
        this.name = trackResponse.getTrackName();
        this.previewUrl = trackResponse.getPreviewUrl();
        this.albumCoverUrl = trackResponse.getAlbumCoverUrl();
        this.artistName = trackResponse.getArtists().getName();
    }
}
