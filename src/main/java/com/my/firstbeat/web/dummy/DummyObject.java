package com.my.firstbeat.web.dummy;

import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DummyObject {

    protected String mockUserPassword = "test1234";

    protected User mockUser(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return User.builder()
                .name("test name")
                .email("test1234@naver.com")
                .password(encoder.encode(mockUserPassword))
                .role(Role.USER)
                .build();
    }
}
