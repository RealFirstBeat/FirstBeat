package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistRetrieveResponse;
import com.my.firstbeat.web.service.PlaylistService;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.my.firstbeat.web.domain.playlist.Playlist;

@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // 플레이리스트 생성
    @PostMapping
    public ResponseEntity<ApiResult<PlaylistCreateResponse>> createPlaylist(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody PlaylistCreateRequest request) {
        return ResponseEntity.ok(ApiResult.success(playlistService.createPlaylist(loginUser.getUser(), request)));
    }

    // 내가 만든 플레이리스트 조회: 최신순 정렬, 페이징
    @GetMapping("/me")
    public ResponseEntity<ApiResult<Page<PlaylistRetrieveResponse>>> getMyPlaylists(
            @AuthenticationPrincipal LoginUser loginUser,
            @PageableDefault(sort = "CreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResult.success(playlistService.getMyPlaylists(loginUser.getUser().getId(), pageable)));
    }

    // 디폴트 플레이리스트 가져오기 또는 생성
    @GetMapping("/default")
    public ResponseEntity<ApiResult<PlaylistResponse>> getDefaultPlaylist(
        @AuthenticationPrincipal LoginUser loginUser) {

        // 디폴트 플레이리스트 가져오기 또는 생성
        Playlist defaultPlaylist = playlistService.getOrCreateDefaultPlaylist(loginUser.getUser().getId());

        // PlaylistResponse 로 변환
        PlaylistResponse response = PlaylistResponse.from(defaultPlaylist);

        return ResponseEntity.ok(ApiResult.success(response));
    }


    // 디폴트 플레이리스트 변경
    @PutMapping("/{playlistId}/default")
    public ResponseEntity<ApiResult<String>> changeDefaultPlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal LoginUser user) {
        playlistService.changeDefaultPlaylist(user.getUser().getId(), playlistId);
        return ResponseEntity.ok(ApiResult.success("디폴트 플레이리스트가 변경되었습니다."));
    }
}