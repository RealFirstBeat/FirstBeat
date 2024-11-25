package com.my.firstbeat.web.ex;

import lombok.Getter;

@Getter
public enum ErrorCode {
    NotFoundUser("Access denied for user data", 403); // UserId를 통해 해당 유저가 존재하는지 하지 않는지 파악하는것을 막기위해 403 코드로 적용
    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
