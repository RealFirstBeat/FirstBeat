package com.my.firstbeat.web.ex;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException{
    private final ErrorCode errorCode;

    public ServerException(ErrorCode errorCode, Throwable e) {
        super(errorCode.getMessage(), e);
        this.errorCode = errorCode;
    }
}
