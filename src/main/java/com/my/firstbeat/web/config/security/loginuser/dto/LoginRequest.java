package com.my.firstbeat.web.config.security.loginuser.dto;

import com.my.firstbeat.web.controller.user.dto.valid.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "유효한 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일을 입력해야 합니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해야 합니다")
    @ValidPassword
    private String password;
}
