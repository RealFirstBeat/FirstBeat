package com.my.firstbeat.web.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;

public class PlaylistServiceTest {

	@InjectMocks
	private PlaylistService playlistService;

	@Mock
	private PlaylistRepository playlistRepository;

	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this); // Mock 객체 초기화
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

}
