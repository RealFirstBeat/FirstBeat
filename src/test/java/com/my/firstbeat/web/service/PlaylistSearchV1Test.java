package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.playlist.dto.response.PaginationInfo;
import com.my.firstbeat.web.controller.playlist.dto.response.PlaylistsData;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.dummy.DummyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.my.firstbeat.web.dummy.DummyObject.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PlaylistSearchV1Test extends DummyObject {

    @InjectMocks
    private PlaylistService playlistService;

    @Mock
    private PlaylistRepository playlistRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("플레이리스트 검색 테스트")
    void getPlaylistsData_Success() {
        // Given
        String query = "Top Hits";
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);
        User user = mockUser();

        // Playlist 객체 생성 시 User 설정
        Playlist playlist1 = new Playlist("Top Hits", "Weekly Top Hits", user);
        Playlist playlist2 = new Playlist("Top Pop", "Top Pop Hits", user);

        Page<Playlist> playlistsPage = new PageImpl<>(List.of(playlist1, playlist2), pageRequest, 2);
        when(playlistRepository.findByTitleContaining(query, pageRequest)).thenReturn(playlistsPage);

        // When
        PlaylistsData result = playlistService.getPlaylistsData(query, page, size);

        // Then
        assertEquals(2, result.getPlaylists().size());
        assertEquals("Top Hits", result.getPlaylists().get(0).getTitle());

        // 페이지네이션 정보 검증
        PaginationInfo pagination = result.getPagination();
        assertEquals(1, pagination.getPage());
        assertEquals(10, pagination.getLimit());
        assertEquals(1, pagination.getTotalPages());
        assertEquals(2, pagination.getTotalItems());
    }
}
