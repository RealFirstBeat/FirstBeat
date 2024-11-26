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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
//락 적용
public class RecommendationService {

    private final SpotifyClient spotifyClient;
    private final UserService userService;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    private final Map<Long, Queue<TrackRecommendationResponse>> userRecommendationsCache = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>(); //유저 별 락
    private static final int REFRESH_THRESHOLD = 5; // 5개 남으면 새로 다시 요청
    private static final int RECOMMENDATIONS_SIZE = 20; //한 번에 받아오는 추천 트랙 수
    private static final int MAX_ATTEMPTS = 20; //추천한 곡이 이미 유저의 플레이리스트에 있는 경우 다시 추천 큐에서 꺼내올 수 있는 최대 횟수

    public TrackRecommendationResponse getRecommendations(Long userId) {
        User user = userService.findByIdOrFail(userId);

        //유저 별 락 획득 (없으면 생성)
        ReentrantLock userLock = userLocks.computeIfAbsent(user.getId(), k -> new ReentrantLock());
        Queue<TrackRecommendationResponse> userRecommendations = userRecommendationsCache.computeIfAbsent(user.getId(), k -> new ConcurrentLinkedQueue<>());

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++){
            userLock.lock();
            try {
                if(userRecommendations.size() <= REFRESH_THRESHOLD) {
                    refreshRecommendations(user);
                }
            } finally {
                userLock.unlock();
            }

            TrackRecommendationResponse recommendation = userRecommendations.poll();
            if(recommendation == null) {
                throw new BusinessException(ErrorCode.FAIL_TO_GET_RECOMMENDATION);
            }

            if(!trackRepository.existsInUserPlaylist(user, recommendation.getSpotifyTrackId())) {
                return recommendation;
            }
            log.debug("유저: {}의 플레이리스트에 추천 트랙: {} 존재. 추천 재시도: {}", user.getId(), recommendation.getSpotifyTrackId(), attempts);
        }
        throw new BusinessException(ErrorCode.NO_NEW_RECOMMENDATIONS_AVAILABLE);
    }

    private void refreshRecommendations(User user){
        String seedGenres = getSeedGenres(user);
        String seedTracks = getSeedTracks(user);

        RecommendationResponse recommendations = spotifyClient.getRecommendations(seedTracks, seedGenres, RECOMMENDATIONS_SIZE);

        Queue<TrackRecommendationResponse> userRecommendations = userRecommendationsCache.computeIfAbsent(
                user.getId(),k -> new ConcurrentLinkedQueue<>());

        recommendations.getTracks()
                .stream()
                .map(TrackRecommendationResponse::new)
                .forEach(userRecommendations::offer);
    }

    private String getSeedGenres(User user){
        String seedGenres = genreRepository.findTop5GenresByUser(user, PageRequest.of(0, 5))
                .stream()
                .map(Genre::getName)
                .collect(Collectors.joining(","));
        if(seedGenres.isEmpty()){
            throw new BusinessException(ErrorCode.GENRES_NOT_FOUND);
        }
        return seedGenres;
    }

    private String getSeedTracks(User user){
        List<Track> trackList = new ArrayList<>(playlistRepository.findAllTrackByUser(user));
        Collections.shuffle(trackList);
        return trackList.stream()
                .limit(5)
                .map(Track::getSpotifyTrackId)
                .collect(Collectors.joining(","));
    }
}
