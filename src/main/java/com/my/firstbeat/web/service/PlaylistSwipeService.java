package com.my.firstbeat.web.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrack;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrackRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSwipeService {

	private final PlaylistService playlistService;
	private final TrackRepository trackRepository;
	private final PlaylistTrackRepository playlistTrackRepository;

	/**
	 * 좋아요 (오른쪽 스와이프)
	 */
	@Transactional
	public String likeTrack(User user, Long spotifyTrackId) {
		Playlist defaultPlaylist = playlistService.getDefaultPlaylist(user.getId());
		Track track = trackRepository.findBySpotifyTrackId(String.valueOf(spotifyTrackId))
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다."));

		// 중복 체크
		boolean trackExists = playlistTrackRepository.existsByPlaylistAndTrack(defaultPlaylist, track);
		if (trackExists) {
			return "곡이 이미 플레이리스트에 존재합니다.";
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
	public String skipTrack(User user, Long spotifyTrackId) {
		Playlist defaultPlaylist = playlistService.getDefaultPlaylist(user.getId());
		Track track = trackRepository.findBySpotifyTrackId(String.valueOf(spotifyTrackId))
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다."));

		// 플레이리스트에서 곡 제거
		PlaylistTrack playlistTrack = (PlaylistTrack)playlistTrackRepository.findByPlaylistAndTrack(defaultPlaylist, track)
			.orElseThrow(() -> new IllegalArgumentException("해당 곡은 플레이리스트에 없습니다."));
		playlistTrackRepository.delete(playlistTrack);

		return "곡이 성공적으로 스킵되었습니다.";
	}
}

