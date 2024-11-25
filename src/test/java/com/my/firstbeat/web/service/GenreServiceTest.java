package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.ex.ErrorCode;
import com.my.firstbeat.client.spotify.ex.SpotifyApiException;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class GenreServiceTest {

    @InjectMocks
    private GenreService genreService;

    @Mock
    private SpotifyClient spotifyClient;

    @Mock
    private GenreRepository genreRepository;


    @Test
    @DisplayName("새로운 장르 추가 테스트: 정상 케이스")
    void updateGenreList_success_WithNewGenres(){

        String[] spotifyGenres = {"rock", "jazz", "kpop"};
        List<Genre> existingGenreList = List.of(new Genre("kpop"));

        when(spotifyClient.getGenres()).thenReturn(spotifyGenres);
        when(genreRepository.findAll()).thenReturn(existingGenreList);

        genreService.updateGenreList();

        //rock과 jazz만 저장되어야 함
        ArgumentCaptor<Collection<Genre>> genreCapture = ArgumentCaptor.forClass(Collection.class);
        verify(genreRepository).saveAll(genreCapture.capture());

        Collection<Genre> savedGenres = genreCapture.getValue();
        assertAll(
                () -> assertEquals(2, savedGenres.size()),
                () -> assertTrue(savedGenres.stream()
                        .map(Genre::getName)
                        .collect(Collectors.toSet())
                        .containsAll(Set.of("rock", "jazz")))
        );
    }

    @Test
    @DisplayName("모든 장르가 이미 존재하는 경우 테스트")
    void updateGenreList_success_WithAllExistingGenres(){

        String[] spotifyGenres = {"rock", "jazz", "kpop"};
        List<Genre> existingGenreList = Arrays.stream(spotifyGenres)
                        .map(Genre::new)
                                .toList();

        when(spotifyClient.getGenres()).thenReturn(spotifyGenres);
        when(genreRepository.findAll()).thenReturn(existingGenreList);

        genreService.updateGenreList();

        verify(genreRepository, never()).saveAll(any());
    }


    @Test
    @DisplayName("Spotify API 호출 중 네트워크 예외 발생 테스트")
    void updateGenreList_fail_WithSpotifyApiException_IOException(){

        when(spotifyClient.getGenres()).thenThrow(new SpotifyApiException(new IOException()));

        SpotifyApiException exception = assertThrows(SpotifyApiException.class,
                () -> genreService.updateGenreList());

        assertAll(
                () -> assertEquals(ErrorCode.NETWORK_ERROR, exception.getErrorCode()),
                () -> verify(genreRepository, never()).saveAll(any())
        );
    }

    @Test
    @DisplayName("Spotify API 호출 중 파싱 예외 발생 테스트")
    void updateGenreList_fail_WithSpotifyApiException_ParseException(){

        when(spotifyClient.getGenres()).thenThrow(new SpotifyApiException(new ParseException()));

        SpotifyApiException exception = assertThrows(SpotifyApiException.class,
                () -> genreService.updateGenreList());

        assertAll(
                () -> assertEquals(ErrorCode.PARSE_ERROR, exception.getErrorCode()),
                () -> verify(genreRepository, never()).saveAll(any())
        );
    }
}