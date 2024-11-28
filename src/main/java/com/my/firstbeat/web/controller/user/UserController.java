package com.my.firstbeat.web.controller.user;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.user.dto.request.UpdateMyPageRequest;
import com.my.firstbeat.web.controller.user.dto.response.GetMyPageResponse;
import com.my.firstbeat.web.controller.user.dto.response.UpdateMyPageResponse;
import com.my.firstbeat.web.service.UserService;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage")
    public ResponseEntity<ApiResult<GetMyPageResponse>> getMyPage(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUser().getId();

        GetMyPageResponse response = userService.getUserData(userId);

        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PatchMapping("/mypage")
    public ResponseEntity<ApiResult<UpdateMyPageResponse>> updateMyPage(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid UpdateMyPageRequest request) {
        Long userId = loginUser.getUser().getId();
        UpdateMyPageResponse response = userService.updateMyPage(userId, request);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
