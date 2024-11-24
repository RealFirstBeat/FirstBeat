package com.my.firstbeat.web.ex;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ;
    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
