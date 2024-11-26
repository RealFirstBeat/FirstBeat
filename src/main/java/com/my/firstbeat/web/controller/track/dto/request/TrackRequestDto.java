package com.my.firstbeat.web.controller.track.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrackRequestDto {

	@NotNull(message = "Spotify Track ID는 필수입니다.")
	private Long spotifyTrackId;
}
