package com.my.firstbeat.web.service;

import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlist.PlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PlaylistSearchV1Test {

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
    void searchPlaylists_Success() {
        // Given
        String query = "Top Hits";
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        Playlist playlist1 = new Playlist("Top Hits", "Weekly Top Hits", null);
        Playlist playlist2 = new Playlist("Top Pop", "Top Pop Hits", null);

        Page<Playlist> playlistsPage = new PageImpl<>(List.of(playlist1, playlist2), pageRequest, 2);
        when(playlistRepository.findByTitleContaining(query, pageRequest)).thenReturn(playlistsPage);

        // When
        Page<Playlist> result = playlistService.searchPlaylists(query, page, size);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("Top Hits", result.getContent().get(0).getTitle());
    }
}
