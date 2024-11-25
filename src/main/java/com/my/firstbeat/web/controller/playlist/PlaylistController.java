package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.service.PlaylistService;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.service.PlaylistService;
import com.my.firstbeat.web.util.api.ApiResult;

@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
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
		@PathVariable Long playlistId,
		@AuthenticationPrincipal LoginUser user) {
		playlistService.changeDefaultPlaylist(user.getUser().getId(), playlistId);
		return ResponseEntity.ok(ApiResult.success("디폴트 플레이리스트가 변경되었습니다."));
	}
}