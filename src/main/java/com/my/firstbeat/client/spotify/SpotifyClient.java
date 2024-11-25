package com.my.firstbeat.client.spotify;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyClient {

    private final SpotifyApi spotifyApi;
    private final SpotifyTokenManager spotifyTokenManager;
    private static final String q = "genre: ";


    // 장르에 따른 트랙 리스트 검색
    public TrackSearchResponse searchTrackList(String genre){
        return executeWithValidToken(() -> {
            Paging<Track> trackPage = spotifyApi.searchTracks(q + genre)
                    .build()
                    .execute();
            log.debug("Spotify API 호출 완료 - searchTracks, genre: {}", genre);
            return new TrackSearchResponse(trackPage);
        });
    }

    //제공하는 장르 목록 검색
    public String[] getGenres(){
        return executeWithValidToken(() -> {
            String[] genres = spotifyApi.getAvailableGenreSeeds()
                    .build()
                    .execute();
            log.debug("Spotify API 호출 완료 - getGenreList, size: {}", genres.length);
            return genres;
        });
    }


    private <T> T executeWithValidToken(SpotifyApiCall<T> apiCall){
        try {
            spotifyApi.setAccessToken(spotifyTokenManager.getValidToken());
            return apiCall.execute();
        } catch (IOException | ParseException | SpotifyWebApiException e){
            log.error("Spotify API 호출 실패: {}", e.getMessage(), e);
            throw new SpotifyApiException(e);
        }
    }

}
