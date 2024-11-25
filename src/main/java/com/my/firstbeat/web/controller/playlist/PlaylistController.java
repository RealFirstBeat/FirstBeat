package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaylistController {


    private final PlaylistService playlistService;

    @PostMapping("/api/v1/playlists")
    public ResponseEntity<PlaylistCreateResponse> createPlaylist(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody PlaylistCreateRequest request) {

        PlaylistCreateResponse response = playlistService.createPlaylist(loginUser, request);
        return ResponseEntity.ok(response);
    }


}
