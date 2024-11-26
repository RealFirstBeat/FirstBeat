package com.my.firstbeat.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.firstbeat.web.domain.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSwipeService {

	private final PlaylistService playlistService;


	/**
	 * 좋아요 (오른쪽 스와이프)
	 */
	@Transactional
	public String likeTrack(User user, Long spotifyTrackId) {
		// 디폴트 플레이리스트에 곡 추가
		return playlistService.addTrackToDefaultPlaylist(user, spotifyTrackId);
	}
}

