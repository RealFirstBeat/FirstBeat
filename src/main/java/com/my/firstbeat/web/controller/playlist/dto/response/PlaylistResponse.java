package com.my.firstbeat.web.controller.playlist.dto.response;

import com.my.firstbeat.web.domain.playlist.Playlist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class PlaylistResponse {
	private Long id;
	private String title;
	private String description;
	private boolean isDefault;

	// Playlist -> PlaylistResponse 변환 정적 메서드
	public static PlaylistResponse from(Playlist playlist) {
		return new PlaylistResponse(
			playlist.getId(),
			playlist.getTitle(),
			playlist.getDescription(),
			playlist.isDefault()
		);
	}

}
