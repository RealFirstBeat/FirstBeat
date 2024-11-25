package com.my.firstbeat.web.controller.user;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.user.dto.response.MyPageResponse;
import com.my.firstbeat.web.service.UserService;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage")
    public ResponseEntity<ApiResult<MyPageResponse>> getMyPage(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUser().getId();

        MyPageResponse response = userService.getUserData(userId);

        return ResponseEntity.ok(ApiResult.success(response));
    }
}
