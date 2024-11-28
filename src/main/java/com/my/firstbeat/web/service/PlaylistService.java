package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistRetrieveResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrackRepository;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
	private final TrackRepository trackRepository;

    // 플레이리스트 생성
    @Transactional
    public PlaylistCreateResponse createPlaylist(User user, PlaylistCreateRequest request) {

        if (playlistRepository.existsByUserAndTitle(user, request.getTitle())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PLAYLIST_TITLE);
        }

        Playlist playlist = new Playlist(
                request.getTitle(),
                request.getDescription(),
                user
        );

        Playlist savedPlaylist = playlistRepository.save(playlist);
        log.debug("Saved playlist: {}", savedPlaylist);

        return new PlaylistCreateResponse(savedPlaylist.getId(), savedPlaylist.getTitle(), savedPlaylist.getDescription());
    }

    // 내가 만든 플레이리스트 조회
    public Page<PlaylistRetrieveResponse> getMyPlaylists(Long userId, Pageable pageable) {
        Page<Playlist> playlists = playlistRepository.findByUserId(userId, pageable);

        if (playlists.isEmpty()) {
            // 플레이리스트가 없는 경우
            throw new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
        // 결과 확인용 로그
        log.debug("플레이리스트 {}개가 조회되었습니다. 사용자: {} ", playlists.getTotalElements(), userId);
        return playlists.map(playlist ->
                new PlaylistRetrieveResponse(playlist.getId(), playlist.getTitle()));
    }

    // 디폴트 플레이리스트 가져오기 또는 생성
	@Transactional
    public Playlist getOrCreateDefaultPlaylist(Long userId) {
        // 디폴트 플레이리스트 검색
        Playlist defaultPlaylist = playlistRepository.findByUserIdAndIsDefault(userId, true)
                .orElse(null);

        // 디폴트 플레이리스트가 없으면 생성
        if (defaultPlaylist == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
	@Transactional
    public void changeDefaultPlaylist(Long userId, Long playlistId) {
        // 최소 한개는 default playlist 가 있다고 가정
        // 기존 유저의 dafault playlist 를 가져와서
        Playlist currentDefaultPlaylist = getOrCreateDefaultPlaylist(userId);
        // 기존 default 해제
        currentDefaultPlaylist.updateDefault(false);
        playlistRepository.save(currentDefaultPlaylist);

        // 파라미터로 넘어온 playlist id 가 진짜 존재하는지 확인
        Playlist newDefaultPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        // 새로운 default 를 세팅
        newDefaultPlaylist.updateDefault(true);
        playlistRepository.save(newDefaultPlaylist);
    }

	public Playlist getDefaultPlaylist(Long userId) {
		return playlistRepository.findByUserIdAndIsDefault(userId, true)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
	}
}

