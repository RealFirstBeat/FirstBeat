package com.my.firstbeat.client.spotify.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    API_ERROR("SPOTIFY_API_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API 호출 중 오류가 발생했습니다"),
    NETWORK_ERROR("SPOTIFY_NETWORK_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "Spotify API 통신 중 네트워크 오류가 발생했습니다"),
    PARSE_ERROR("SPOTIFY_PARSE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API 응답 파싱 중 오류가 발생했습니다"),
    AUTH_ERROR("SPOTIFY_AUTH_ERROR", HttpStatus.UNAUTHORIZED, "Spotify API 인증 오류가 발생했습니다"),
    RATE_LIMIT_ERROR("SPOTIFY_RATE_LIMIT_ERROR", HttpStatus.TOO_MANY_REQUESTS, "Spotify API 호출 한도를 초과했습니다"),
    UNKNOWN_ERROR("SPOTIFY_UNKNOWN_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다"),
    INTERNAL_SERVER_ERROR("UNEXPECTED_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 내부 오류가 발생했습니다");


    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
