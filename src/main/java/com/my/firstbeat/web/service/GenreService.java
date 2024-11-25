package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GenreService {

    private final SpotifyClient spotifyClient;
    private final GenreRepository genreRepository;

    //장르 데이터 세팅
    @Transactional
    public void updateGenreList(){
        //Spotify 제공 장르 목록 조회
        HashSet<String> spotifyGenres = new HashSet<>(Arrays.asList(spotifyClient.getGenres()));

        //기존 DB에 있는 장르 목록 조회
        Set<String> existingGenres = genreRepository.findAll().stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());

        //새로 추가된 장르만 필터링
        Set<String> newGenres = spotifyGenres.stream()
                .filter(genre -> !existingGenres.contains(genre))
                .collect(Collectors.toSet());

        if(!newGenres.isEmpty()){
            List<Genre> genreList = newGenres.stream()
                    .map(Genre::new)
                    .toList();
            genreRepository.saveAll(genreList);
            log.info("Spotify 새로운 장르 추가: {}개", newGenres.size());
        } else {
            log.info("새로 추가된 Spotify 장르가 없습니다");
        }
    }

}
