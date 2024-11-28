package com.my.firstbeat.web.controller.track;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.service.recommemdation.RecommendationService;
import com.my.firstbeat.web.service.recommemdation.RecommendationServiceWithRedis;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrackController {

    private final RecommendationService recommendationService;
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
}
