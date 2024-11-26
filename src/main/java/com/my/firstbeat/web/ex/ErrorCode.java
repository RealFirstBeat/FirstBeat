package com.my.firstbeat.web.ex;

import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATE_PLAYLIST_TITLE("이미 존재하는 제목입니다.", HttpStatus.SC_CONFLICT),
    PLAYLIST_NOT_FOUND("플레이리스트가 존재하지 않습니다.", HttpStatus.SC_NOT_FOUND);

    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
