package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.SpotifyClient;
import com.my.firstbeat.client.spotify.dto.response.RecommendationResponse;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationServiceWithoutLock {

    private final SpotifyClient spotifyClient;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    private final Map<Long, Queue<TrackRecommendationResponse>> userRecommendationsCache = new ConcurrentHashMap<>();
    private static final int REFRESH_THRESHOLD = 5; // 5개 남으면 새로 다시 요청
    private static final int RECOMMENDATIONS_SIZE = 20; //한 번에 받아오는 추천 트랙 수
    private static final int MAX_ATTEMPTS = 20; //추천한 곡이 이미 유저의 플레이리스트에 있는 경우 다시 추천 큐에서 꺼내올 수 있는 최대 횟수

    private static final int SEED_MAX = 5;

    public TrackRecommendationResponse getRecommendations(Long userId) {
        //사용자 검증
        User user = userService.findByIdOrFail(userId);

        //해당 유저의 추천 트랙 가져오기
        Queue<TrackRecommendationResponse> userRecommendations = userRecommendationsCache.computeIfAbsent(user.getId(), k -> new ConcurrentLinkedQueue<>());

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++){
            //비어있거나 5개 이하면 다시 spotifyClient 로 요청
            //TODO 이거 스레드 동시성 처리하면 트레이드 오프가 있는지
            if(userRecommendations.size() <= REFRESH_THRESHOLD) {
                refreshRecommendations(user);
            }

            TrackRecommendationResponse trackRecommendationResponse = userRecommendations.poll();
            if(trackRecommendationResponse == null){
                throw new BusinessException(ErrorCode.FAIL_TO_GET_RECOMMENDATION);
            }

            //추천하려는 장르가 이미 유저의 플레이리스트에 있는 곡이라면 패스
            if(!trackRepository.existsInUserPlaylist(user, trackRecommendationResponse.getSpotifyTrackId())){
                return trackRecommendationResponse;
            }

            log.debug("유저: {}의 플레이리스트에 추천 트랙: {} 존재. 추천 재시도: {}",
                    user.getId(), trackRecommendationResponse.getSpotifyTrackId(), attempts);
        }
        throw new BusinessException(ErrorCode.NO_NEW_RECOMMENDATIONS_AVAILABLE);
    }

    private void refreshRecommendations(User user){
        //사용자 선호 장르 조회 (최대 5개)
        String seedGenres = getSeedGenres(user);

        //사용자의 모든 플레이리스트에 있는 트랙 총 5개 랜덤 조회
        String seedTracks = getSeedTracks(user);

        //해당 장르와 해당 트랙 전달
        RecommendationResponse recommendations = spotifyClient.getRecommendations(seedTracks, seedGenres, RECOMMENDATIONS_SIZE);

        //추천 트랙 리스트 추가
        Queue<TrackRecommendationResponse> userRecommendations = userRecommendationsCache.computeIfAbsent(
                user.getId(),k -> new ConcurrentLinkedQueue<>());

        recommendations.getTracks()
                .stream()
                .map(TrackRecommendationResponse::new)
                .forEach(userRecommendations::offer);
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
