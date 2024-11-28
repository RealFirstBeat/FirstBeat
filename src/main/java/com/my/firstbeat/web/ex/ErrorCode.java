package com.my.firstbeat.web.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    
    USER_NOT_FOUND("존재하지 않는 사용자입니다", HttpStatus.NOT_FOUND.value()),
    FAIL_TO_GET_RECOMMENDATION("추천 트랙을 가져오는데 실패했습니다", HttpStatus.NO_CONTENT.value()),
    GENRES_NOT_FOUND("사용자의 선호 장르가 존재하지 않습니다", HttpStatus.NOT_FOUND.value()),
    NO_NEW_RECOMMENDATIONS_AVAILABLE("현재 추천 가능한 새로운 곡이 없습니다. 잠시 후 다시 시도해주세요", HttpStatus.NOT_FOUND.value()),
    DUPLICATE_PLAYLIST_TITLE("이미 존재하는 제목입니다.", HttpStatus.CONFLICT.value()),
    MAX_RECOMMENDATION_ATTEMPTS_EXCEED("일시적으로 추천 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요", HttpStatus.CONFLICT.value()),

    PLAYLIST_NOT_FOUND("플레이리스트가 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),

    SERVICE_TEMPORARY_UNAVAILABLE("추천 서비스가 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요", HttpStatus.SERVICE_UNAVAILABLE.value()),

    TRACK_FETCH_ERROR("플레이리스트 내 트랙 목록을 불러오는 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
