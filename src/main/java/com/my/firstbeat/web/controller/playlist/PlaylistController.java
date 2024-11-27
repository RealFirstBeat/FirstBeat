package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.TrackListResponse;
import com.my.firstbeat.web.service.PlaylistService;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.service.PlaylistService;
import com.my.firstbeat.web.util.api.ApiResult;

@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
@Validated
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<ApiResult<PlaylistCreateResponse>> createPlaylist(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody PlaylistCreateRequest request) {
        return ResponseEntity.ok(ApiResult.success(playlistService.createPlaylist(loginUser.getUser(), request)));
    }

	// 디폴트 플레이리스트 가져오기 또는 생성
	@GetMapping("/default")
	public ResponseEntity<ApiResult<Playlist>> getDefaultPlaylist(
		@AuthenticationPrincipal LoginUser user) {

		// 현재 로그인한 사용자의 디폴트 플레이리스트 가져오기 또는 생성
		Playlist defaultPlaylist = playlistService.getOrCreateDefaultPlaylist(user.getUser().getId());
		return ResponseEntity.ok(ApiResult.success(defaultPlaylist));
	}

	// 디폴트 플레이리스트 변경
	@PutMapping("/{playlistId}/default/")
	public ResponseEntity<ApiResult<String>> changeDefaultPlaylist(
		@PathVariable(value = "playlistId") Long playlistId,
		@AuthenticationPrincipal LoginUser user) {
		playlistService.changeDefaultPlaylist(user.getUser().getId(), playlistId);
		return ResponseEntity.ok(ApiResult.success("디폴트 플레이리스트가 변경되었습니다."));
	}

	@GetMapping("/{playlistId}")
	public ResponseEntity<ApiResult<TrackListResponse>> getTrackList(
			@PathVariable(value = "playlistId") Long playlistId,
		    @RequestParam(value = "page", defaultValue = "0", required = false) @PositiveOrZero int page,
		    @RequestParam(value = "size", defaultValue = "10", required = false) @Range(min = 1, max = 100, message = "페이지 크기는 1에서 100 사이여야 합니다") int size) {
		return ResponseEntity.ok(ApiResult.success(playlistService.getTrackList(playlistId, page, size)));
	}
}