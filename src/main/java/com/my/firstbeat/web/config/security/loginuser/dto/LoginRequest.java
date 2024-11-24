package com.my.firstbeat.web.config.security.loginuser.dto;

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
