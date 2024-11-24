package com.my.firstbeat.client.spotify;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

//스포티파이 api 호출 관리
@FunctionalInterface
public interface SpotifyApiCall<T>{
    T execute() throws IOException, ParseException, SpotifyWebApiException;
}
