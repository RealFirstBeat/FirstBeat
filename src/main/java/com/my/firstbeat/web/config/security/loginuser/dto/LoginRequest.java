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

    private String email;

    private String password;
}
