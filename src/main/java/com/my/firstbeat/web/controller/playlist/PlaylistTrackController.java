package com.my.firstbeat.web.controller.playlist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.track.dto.request.TrackRequestDto;
import com.my.firstbeat.web.service.PlaylistSwipeService;
import com.my.firstbeat.web.util.api.ApiResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playlist-tracks")
public class PlaylistTrackController {

	private final PlaylistSwipeService playlistSwipeService;

	/**
	 * 좋아요 (오른쪽 스와이프)
	 * 곡을 "좋아요 곡" 플레이리스트에 추가
	 */
	@PostMapping("/like")
	public ResponseEntity<ApiResult<String>> likeTrack(
		@AuthenticationPrincipal LoginUser loginUser,
		@Valid @RequestBody TrackRequestDto requestDto) {

		String result = playlistSwipeService.likeTrack(loginUser.getUser(), requestDto.getSpotifyTrackId());
		return ResponseEntity.ok(ApiResult.success(result));
	}
}
