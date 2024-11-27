package com.my.firstbeat.web.service;


import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationServiceWithoutCache {

    private final SpotifyClient spotifyClient;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    private static final int MAX_ATTEMPTS = 20; //추천한 곡이 이미 유저의 플레이리스트에 있는 경우 다시 추천 큐에서 꺼내올 수 있는 최대 횟수
    private static final int SEED_MAX = 5;
    private static final int RECOMMENDATIONS_SIZE = 1; //한 번에 받아오는 추천 트랙 수


    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);
        String seedGenres = getSeedGenres(user);

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++){
            RecommendationResponse recommendation = spotifyClient.getRecommendations(
                    seedGenres,
                    getSeedTracks(user),
                    RECOMMENDATIONS_SIZE
            );

            List<TrackResponse> tracks = recommendation.getTracks();
            if (tracks == null || tracks.isEmpty()) {
                continue;
            }

            TrackResponse trackResponse = tracks.get(0);
            if (trackResponse != null && !trackRepository.existsInUserPlaylist(user, trackResponse.getId())) {
                return new TrackRecommendationResponse(trackResponse);
            }
        }
        throw new BusinessException(ErrorCode.MAX_RECOMMENDATION_ATTEMPTS_EXCEED);
    }


    private String getSeedGenres(User user){
        String seedGenres = genreRepository.findTop5GenresByUser(user, PageRequest.of(0, SEED_MAX))
                .stream()
                .map(Genre::getName)
                .collect(Collectors.joining(","));
        if(seedGenres.isEmpty()){
            throw new BusinessException(ErrorCode.GENRES_NOT_FOUND);
        }
        return seedGenres;
    }


    private String getSeedTracks(User user){
        List<Track> trackList = playlistRepository.findAllTrackByUser(user, PageRequest.of(0, SEED_MAX));
        return trackList.stream()
                .limit(SEED_MAX)
                .map(Track::getSpotifyTrackId)
                .collect(Collectors.joining(","));
    }
}
