package com.my.firstbeat.client.spotify.ex;

import lombok.Getter;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

@Getter
public class SpotifyApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public SpotifyApiException(Throwable e) {
        super(e.getMessage(), e);
        this.errorCode = determineErrorCode(e);
    }

    private ErrorCode determineErrorCode(Throwable e){
        if(e instanceof SpotifyWebApiException){
            String message = e.getMessage().toLowerCase();
            if(message.contains("token")){
                return ErrorCode.AUTH_ERROR;
            }else if(message.contains("rate limit")){
                return ErrorCode.RATE_LIMIT_ERROR;
            }
            return ErrorCode.API_ERROR;
        }
        if(e instanceof IOException){
            return ErrorCode.NETWORK_ERROR;
        }
        if(e instanceof ParseException){
            return ErrorCode.PARSE_ERROR;
        }
        return ErrorCode.UNKNOWN_ERROR;
    }


}
