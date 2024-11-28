package com.my.firstbeat.web.controller.track;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.track.dto.request.TrackRequestDto;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.service.PlaylistSwipeService;
import com.my.firstbeat.web.service.recommemdation.RecommendationService;
import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithRedis;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrackController {

    private final RecommendationService recommendationService;
    private final PlaylistSwipeService playlistSwipeService;
    private final RecommendationServiceWithRedis recommendationServiceWithRedis;

    @GetMapping("/v2/tracks/recommendations")
    public ResponseEntity<ApiResult<TrackRecommendationResponse>> getRecommendationsV2(
            @AuthenticationPrincipal LoginUser loginUser){
        return ResponseEntity.ok(ApiResult.success(recommendationService.getRecommendations(loginUser.getUser().getId())));
    }

    @GetMapping("/v3/tracks/recommendations")
    public ResponseEntity<ApiResult<TrackRecommendationResponse>> getRecommendationV3(
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(ApiResult.success(recommendationServiceWithRedis.getRecommendations(loginUser.getUser().getId())));
    }
	/**
	 * 좋아요 (오른쪽 스와이프)
	 * 곡을 "좋아요 곡" 플레이리스트에 추가
	 */
	@PostMapping("/tracks/like")
	public ResponseEntity<ApiResult<String>> likeTrack(
		@AuthenticationPrincipal LoginUser loginUser,
		@Valid @RequestBody TrackRequestDto requestDto) {

		String result = playlistSwipeService.likeTrack(loginUser.getUser(), requestDto.getSpotifyTrackId());
		return ResponseEntity.ok(ApiResult.success(result));
	}

	/**
	 * 곡 스킵 (왼쪽 스와이프)
	 */
	@PostMapping("/tracks/skip")
	public ResponseEntity<ApiResult<String>> skipTrack(
		@AuthenticationPrincipal LoginUser loginUser,
		@RequestBody TrackRequestDto requestDto) {

		String result = playlistSwipeService.skipTrack(loginUser.getUser(), requestDto.getSpotifyTrackId());
		return ResponseEntity.ok(ApiResult.success(result));
	}
}
