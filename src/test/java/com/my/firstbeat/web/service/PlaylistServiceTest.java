package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;
import com.my.firstbeat.web.controller.playlist.dto.response.TrackListResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistRetrieveResponse;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest extends DummyObject {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistService playlistService;

  	@Mock
	private UserRepository userRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private SearchService searchService;

  	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this); // Mock 객체 초기화
	}

    @Test
    @DisplayName("플레이리스트 생성: 정상")
    void createPlaylist_success() {
        // Given
        User mockUser = mockUser();

        Playlist playlist = new Playlist("Test Playlist", "Test Description", mockUser);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);
        PlaylistCreateRequest request = new PlaylistCreateRequest("Test Playlist", "Test Description");

        // When
        PlaylistCreateResponse response = playlistService.createPlaylist(mockUser, request);

        // Then
        assertNotNull(response);
        assertEquals("Test Playlist", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("플레이리스트 생성 실패: 중복 타이틀")
    void createPlaylist_duplicateTitle() {
        // Given
        User mockUser = mockUser();
        Playlist excistingPlaylist = new Playlist("Duplicate Title", "Test Description", mockUser);
        when(playlistRepository.existsByUserAndTitle(mockUser, "Duplicate Title")).thenReturn(true);

        PlaylistCreateRequest request = new PlaylistCreateRequest("Duplicate Title", "New Description");

        // When & Then
        assertThrows(BusinessException.class, () -> playlistService.createPlaylist(mockUser, request));
    }

    @Test
    @DisplayName("내가 만든 플레이리스트 조회: 성공")
    void getMyPlaylists_success() {
        // Given
        User mockUser = mockUserWithId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        List<Playlist> mockPlaylists = List.of(
                new Playlist("My Playlists 1", "Playlist 1", mockUser),
                new Playlist("My Playlists 2", "Playlist 2", mockUser)
        );
        Page<Playlist> mockPage = new PageImpl<>(mockPlaylists, pageable, mockPlaylists.size());
        Mockito.when(playlistRepository.findByUserId(mockUser.getId(), pageable)).thenReturn(mockPage);

        // When
        Page<PlaylistRetrieveResponse> result = playlistService.getMyPlaylists(mockUser.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("My Playlists 1", result.getContent().get(0).getTitle());
        assertEquals("My Playlists 2", result.getContent().get(1).getTitle());
        Mockito.verify(playlistRepository).findByUserId(mockUser.getId(), pageable);
    }

    @Test
    @DisplayName("내가 만든 플레이리스트 조회: 존재하지 않는 경우")
    void getMyPlaylists_isEmpty() {
        // Given
        User mockUser = mockUserWithId(1L);
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(playlistRepository.findByUserId(mockUser.getId(), pageable)).thenReturn(Page.empty());

        // When & Then
        BusinessException e = assertThrows(
                BusinessException.class, () -> playlistService.getMyPlaylists(mockUser.getId(), pageable)
        );

        assertEquals(ErrorCode.PLAYLIST_NOT_FOUND, e.getErrorCode());
        Mockito.verify(playlistRepository).findByUserId(mockUser.getId(), pageable);
    }

    @Test
    @DisplayName("디폴트 플레이리스트가 없으면 생성")
    void getOrCreateDefaultPlaylist_success_WithNewDefaultPlaylist() {
        // Given
        Long userId = 1L;
        User user = new User(userId, "test@example.com", "Test User", "password", null);

        when(playlistRepository.findByUserIdAndIsDefault(userId, true)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Playlist result = playlistService.getOrCreateDefaultPlaylist(userId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isDefault()),
                () -> assertEquals("제목없음", result.getTitle())
        );

        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("디폴트 플레이리스트가 이미 존재")
    void getOrCreateDefaultPlaylist_shouldReturnExistingDefaultPlaylist() {
        // Given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .role(Role.USER)
                .build();
        Playlist existingPlaylist = new Playlist(user, "제목없음", "기본 설명", true);

        when(playlistRepository.findByUserIdAndIsDefault(userId, true))
                .thenReturn(Optional.of(existingPlaylist));

        // When
        Playlist result = playlistService.getOrCreateDefaultPlaylist(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isDefault());
        assertEquals("제목없음", result.getTitle());
        verify(playlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("디폴트 플레이리스트 변경 성공")
    void changeDefaultPlaylist_success_WithValidPlaylistId() {
        // Given
        Long userId = 1L;
        Long newPlaylistId = 2L;

        User mockUser = mock(User.class); // Mock User 객체 생성
        Playlist currentDefault = new Playlist(mockUser, "현재 디폴트", "기존 설명", true);
        Playlist newDefault = new Playlist(mockUser, "새로운 디폴트", "새로운 설명", false);

        when(playlistRepository.findByUserIdAndIsDefault(userId, true)).thenReturn(Optional.of(currentDefault));
        when(playlistRepository.findById(newPlaylistId)).thenReturn(Optional.of(newDefault));

        // When
        playlistService.changeDefaultPlaylist(userId, newPlaylistId);

        // Then
        assertAll(
                () -> assertFalse(currentDefault.isDefault()),
                () -> assertTrue(newDefault.isDefault())
        );
        verify(playlistRepository).save(currentDefault);
        verify(playlistRepository).save(newDefault);
    }


	@Test
	@DisplayName("플레이리스트 내 추천 트랙 반환: 정상 케이스")
	void getTrackList_success(){
	  int page = 0;
	  int size = 2;
		Pageable pageable = PageRequest.of(page, size);

	  Long playlistId = 1L;
	  Playlist playlist = Playlist.builder()
			  .title("나만의 플레이리스트")
			  .id(playlistId)
			  .description("내꺼")
			  .build();

	  Page<Track> trackPage = new PageImpl<>(
			  Arrays.asList(
					  Track.builder()
							  .name("노래 제목")
							  .spotifyTrackId("spotifyTrackId")
							  .albumCoverUrl("url")
							  .previewUrl("url")
							  .artistName("NctWish")
							  .build(),
					  Track.builder()
							  .name("노래 제목1")
							  .spotifyTrackId("spotifyTrackId1")
							  .albumCoverUrl("url1")
							  .previewUrl("url1")
							  .artistName("NctWish1")
							  .build()
			  ),
			  pageable,
			  2
	  );
		PlaylistService realService = new PlaylistService(playlistRepository, userRepository, trackRepository, searchService);
		PlaylistService spyService = spy(realService);

		doReturn(playlist).when(spyService).findByIdOrFail(playlistId);
		when(trackRepository.findAllByPlaylist(playlist, pageable)).thenReturn(trackPage);

		  //when
	  TrackListResponse results = spyService.getTrackList(playlistId, page, size);

	  assertNotNull(results);
	  verify(trackRepository, times(1)).findAllByPlaylist(eq(playlist), any(Pageable.class));
	  assertEquals(2, results.getTracks().size());
	  assertEquals("노래 제목", results.getTracks().get(0).getTrackName());
	  assertEquals("노래 제목1", results.getTracks().get(1).getTrackName());
	}


	@Test
	@DisplayName("플레이리스트 내 추천 트랙 반환: 존재하지 않는 플레이리스트 조회 시 예외 발생")
	void getTrackList_PlaylistNotFound() {
		Long playlistId = 999L;
		PlaylistService realService = new PlaylistService(playlistRepository, userRepository, trackRepository, searchService);
		PlaylistService spyService = spy(realService);
		doThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND))
				.when(spyService).findByIdOrFail(playlistId);


		assertThrows(BusinessException.class, () ->
				spyService.getTrackList(playlistId, 0, 10));

		verify(trackRepository, never()).findAllByPlaylist(any(), any());
	}

	@Test
	@DisplayName("플레이리스트 내 추천 트랙 반환: 트랙 조회 중 에러 발생 시 BusinessException throw")
	void getTrackList_TrackFetchError() {
		Long playlistId = 1L;
		Playlist playlist = Playlist.builder()
				.title("나만의 플레이리스트")
				.id(playlistId)
				.description("내꺼")
				.build();

		PlaylistService realService = new PlaylistService(playlistRepository, userRepository, trackRepository, searchService);
		PlaylistService spyService = spy(realService);

		doReturn(playlist).when(spyService).findByIdOrFail(playlistId);
		when(trackRepository.findAllByPlaylist(any(), any()))
				.thenThrow(new RuntimeException("DB 에러"));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () ->
				spyService.getTrackList(playlistId, 0, 10));

		assertEquals(ErrorCode.TRACK_FETCH_ERROR, exception.getErrorCode());
		verify(trackRepository).findAllByPlaylist(any(), any());
	}
}

