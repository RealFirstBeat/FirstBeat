package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.TrackListResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistRetrieveResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
  	private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final SearchService searchService;


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
        log.debug("저장된 플레이리스트: {}", savedPlaylist.getTitle());

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


    public TrackListResponse getTrackList(Long playlistId, int page, int size) {
        Playlist playlist = findByIdOrFail(playlistId);
        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<Track> trackPage = trackRepository.findAllByPlaylist(playlist, pageable);
            return new TrackListResponse(trackPage);
        } catch (Exception e) {
            log.error("플레이리스트 내 트랙 목록 조회 시 오류 발생: 플레이리스트 ID: {}, 원인: {}", playlistId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.TRACK_FETCH_ERROR);
        }
    }

    public Playlist findByIdOrFail(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
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

    public Page<Playlist> searchPlaylists(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return playlistRepository.findByTitleContaining(query, pageable);
    }

    // 인메모리 캐시 사용
    @Cacheable(value = "simpleCache", cacheManager = "inMemoryCacheManager", key = "#query")
    public Page<Playlist> searchPlaylistsWithInMemoryCache(String query, int page, int size) {
        if (!query.isEmpty()) {
            searchService.recordSearch(query); // 검색어 기록
        }
        Pageable pageable = PageRequest.of(page, size);
        return playlistRepository.findByTitleContaining(query, pageable);
    }

    @Transactional
    public void deletePlaylist(Long userId, Long playlistId) {
        // Playlist 존재 여부 확인
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        // Playlist의 소유자 검증
        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND); // 소유자가 아님
        }

        // Default Playlist인지 확인
        if (playlist.isDefault()) {
            throw new BusinessException(ErrorCode.CAN_NOT_DELETE_DEFAULT_PLAYLIST); // 디폴트 플레이리스트는 삭제 불가
        }

        // 삭제
        playlistRepository.delete(playlist);
        log.info("플레이리스트가 삭제되었습니다.", playlistId);
    }
}

