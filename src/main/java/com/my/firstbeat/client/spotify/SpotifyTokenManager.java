package com.my.firstbeat.client.spotify;

import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SpotifyTokenManager {

    private final SpotifyApi spotifyApi;

    private volatile String currentToken;

    private volatile LocalDateTime tokenExpiryTime;

    private final Object lock = new Object();

    @Value("${spotify.token.refresh-buffer-seconds}")
    private int refreshBufferSeconds; //5분 전에 갱신하도록


    public String getValidToken(){
        if(isTokenExpiringSoon()){
            synchronized (lock){
                if(isTokenExpiringSoon()){
                    refreshToken();
                }
            }
        }
        return currentToken;
    }

    private void refreshToken() {
        try {
            ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
            currentToken = credentials.getAccessToken();
            tokenExpiryTime = LocalDateTime.now().plusSeconds(credentials.getExpiresIn());
            spotifyApi.setAccessToken(currentToken);
            log.debug("Spotify 토큰 갱신 완료. 만료 시간: {}", tokenExpiryTime);
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage(), e);
            throw new SpotifyApiException(e);
        }
    }

    private boolean isTokenExpiringSoon(){
        return tokenExpiryTime == null || LocalDateTime.now().plusSeconds(refreshBufferSeconds).isAfter(tokenExpiryTime);
    }


}
