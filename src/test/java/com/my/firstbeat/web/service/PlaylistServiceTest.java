package com.my.firstbeat.web.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.ex.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.controller.playlist.dto.request.PlaylistCreateRequest;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistCreateResponse;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest extends DummyObject {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    @DisplayName("플레이리스트 생성: 정상")
    void createPlaylist_success() {
        // Given
        User mockUser = mockUser();

        Playlist playlist = new Playlist("Test Playlist", "Test Description", mockUser);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);
        PlaylistCreateRequest request = new PlaylistCreateRequest("Test Playlist", "Test Description");

        // When
        PlaylistCreateResponse response = playlistService.createPlaylist(new LoginUser(mockUser), request);

        // Then
        assertNotNull(response);
        assertEquals("Test Playlist", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("플레이리스트 생성 실패: 필수 값 누락")
    void createPlaylist_missingFields() {
        // Given
        PlaylistCreateRequest request = new PlaylistCreateRequest("","");

        // When & Then
        assertThrows(BusinessException.class, () -> playlistService.createPlaylist(new LoginUser(mockUser()), request));
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
        assertThrows(BusinessException.class, () -> playlistService.createPlaylist(new LoginUser(mockUser), request));
    }


}