package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TrackService {

    private final SpotifyClient spotifyClient;


    public TrackSearchResponse searchTrackList(String genre){
        try {

            //실제로 제공하는 장르인지 확인 -> genreService 사용

            //스포티파이에 조회
            TrackSearchResponse trackSearchResponse = spotifyClient.searchTrackList(genre);
            log.debug("장르 '{}' 기반 트랙 검색 완료 - {}개 트랙 조회됨", genre, trackSearchResponse.getTotal());
            return trackSearchResponse;
        }catch (SpotifyApiException e){
            log.error("트랙 검색 중 오류 발생 - 장르: {}", genre);
            throw e;
        }
    }
}
