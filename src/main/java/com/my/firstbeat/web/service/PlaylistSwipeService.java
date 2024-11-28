package com.my.firstbeat.web.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrack;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrackRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSwipeService {

	private final PlaylistService playlistService;
	private final TrackRepository trackRepository;
	private final PlaylistTrackRepository playlistTrackRepository;
	private final RecommendationService recommendationService;

	/**
	 * 좋아요 (오른쪽 스와이프)
	 */
	@Transactional
	public String likeTrack(User user, String spotifyTrackId) {
		Playlist defaultPlaylist = playlistService.getDefaultPlaylist(user.getId());
		Track track = trackRepository.findBySpotifyTrackId(spotifyTrackId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

		// 중복 체크
		boolean trackExists = playlistTrackRepository.existsByPlaylistAndTrack(defaultPlaylist, track);
		if (trackExists) {
			throw new BusinessException(ErrorCode.DUPLICATE_PLAYLIST_TITLE);
		}

		// 곡 추가
		PlaylistTrack playlistTrack = new PlaylistTrack(defaultPlaylist, track);
		playlistTrackRepository.save(playlistTrack);

		return "곡이 성공적으로 플레이리스트에 추가되었습니다";
	}
	/**
	 * 곡 스킵 (왼쪽 스와이프)
	 */
	@Transactional
	public String skipTrack (User user, String spotifyTrackId) {
		// 추천 트랙 리스트에서 트랙을 가져옴
		TrackRecommendationResponse recommendation = recommendationService.getRecommendations(user.getId());

		// 스킵할 트랙인지 확인
		if (!recommendation.getSpotifyTrackId().equals(spotifyTrackId)) {
			throw new BusinessException(ErrorCode.NO_NEW_RECOMMENDATIONS_AVAILABLE);
		}

		// 추천 트랙에서 해당 트랙을 제거
		boolean removed = recommendationService.removeTrackFromRecommendations(user.getId(), spotifyTrackId);

		if (!removed) {
			throw new BusinessException(ErrorCode.RECOMMENDATION_TRACK_REMOVAL_FAILED);
		}

		log.info("추천 트랙이 성공적으로 스킵되었습니다. User ID: {}, Track ID: {}", user.getId(), spotifyTrackId);

		return "추천 트랙이 성공적으로 스킵되었습니다.";
	}
}

