package com.my.firstbeat.client.spotify;

import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyClient {

    private final SpotifyApi spotifyApi;
    private final SpotifyTokenManager spotifyTokenManager;



    private <T> T executeWithValidToken(SpotifyApiCall<T> apiCall, String errorMsg){
        try {
            spotifyApi.setAccessToken(spotifyTokenManager.getValidToken());
            return apiCall.execute();
        } catch (Exception e){
            log.error(errorMsg);
            throw new SpotifyApiException(e);
        }
    }

}
