package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDeleteResponse {
	private Long playlistId;
	private Long trackId;
	private String message;
}