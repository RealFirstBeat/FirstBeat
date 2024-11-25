package com.my.firstbeat.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaylistService {
	private final PlaylistRepository playlistRepository;
	private final UserRepository userRepository;

	// 디폴트 플레이리스트 가져오기 또는 생성
	public Playlist getOrCreateDefaultPlaylist(Long userId) {
		// 디폴트 플레이리스트 검색
		Playlist defaultPlaylist = playlistRepository.findByUserIdAndIsDefault(userId, true)
			.orElse(null);

		// 디폴트 플레이리스트가 없으면 생성
		if (defaultPlaylist == null) {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
			defaultPlaylist = playlistRepository.save(new Playlist(
				user,
				"제목없음",
				"자동으로 생성된 기본 플레이리스트",
				true
			));
		}
		return defaultPlaylist;
	}

	//디폴트 플레이리스트 변경 로직
	public void changeDefaultPlaylist(Long userId, Long playlistId) {
		// 최소 한개는 default playlist 가 있다고 가정
		// 기존 유저의 dafault playlist 를 가져와서
		Playlist currentDefaultPlaylist = getOrCreateDefaultPlaylist(userId);
		// 기존 default 해제
		currentDefaultPlaylist.updateDefault(false);
		playlistRepository.save(currentDefaultPlaylist);

		// 파라미터로 넘어온 playlist id 가 진짜 존재하는지 확인
		Playlist newDefaultPlaylist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new IllegalArgumentException("플레이리스트가 존재하지 않습니다."));

		// 새로운 defalut를 세팅
		newDefaultPlaylist.updateDefault(true);
		playlistRepository.save(newDefaultPlaylist);
	}
}

