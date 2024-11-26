package com.my.firstbeat.web.ex;

import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("Access denied for user data", HttpStatus.SC_FORBIDDEN), // UserId를 통해 해당 유저가 존재하는지 하지 않는지 파악하는것을 막기위해 403 코드로 적용
    DUPLICATE_PLAYLIST_TITLE("이미 존재하는 제목입니다.", HttpStatus.SC_CONFLICT),
    ;

    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
