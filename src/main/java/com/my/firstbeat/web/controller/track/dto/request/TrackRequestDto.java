package com.my.firstbeat.web.controller.track.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrackRequestDto {

	@NotNull(message = "Spotify Track ID는 필수입니다.")
	private String spotifyTrackId;

	private String name; //곡 제목
	private String previewUrl; //프리뷰 url
	private String albumCoverUrl; //앨범 커버 url
	private String artistName; //가수 이름
}
