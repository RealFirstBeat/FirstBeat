package com.my.firstbeat.client.spotify.handler;


import com.my.firstbeat.client.spotify.ex.ErrorCode;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class SpotifyExceptionHandler {

    @ExceptionHandler(SpotifyApiException.class)
    public ResponseEntity<ApiResult<String>> handleSpotifyApiException(SpotifyApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        String detailMessage = e.getCause() != null ? e.getCause().getMessage() : null;

        log.error("[{}] Spotify 오류 발생: {}", errorCode.getCode(), e.getMessage(), e);

        return new ResponseEntity<>(
                ApiResult.error(errorCode.getStatus().value(), errorCode.getMessage(), detailMessage),
                errorCode.getStatus());
    }

}
