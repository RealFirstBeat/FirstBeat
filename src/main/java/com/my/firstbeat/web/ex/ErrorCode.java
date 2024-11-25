package com.my.firstbeat.web.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUNT("존재하지 않는 사용자입니다", HttpStatus.NOT_FOUND.value()),
    FAIL_TO_GET_RECOMMENDATION("추천 트랙을 가져오는데 실패했습니다", HttpStatus.NO_CONTENT.value()),
    GENRES_NOT_FOUND("사용자의 선호 장르가 존재하지 않습니다", HttpStatus.NOT_FOUND.value()),
    NO_NEW_RECOMMENDATIONS_AVAILABLE("현재 추천 가능한 새로운 곡이 없습니다. 잠시 후 다시 시도해주세요", HttpStatus.NOT_FOUND.value())


    ;



    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
