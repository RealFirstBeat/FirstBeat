package com.my.firstbeat.web.config.security.loginuser.dto;

import com.my.firstbeat.web.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginResponse {
    public static final String SUCCESS = "로그인이 성공적으로 처리되었습니다";
    private Long id;
    private String message;

    public LoginResponse(User user) {
        this.id = user.getId();
        this.message = SUCCESS;
    }
}
