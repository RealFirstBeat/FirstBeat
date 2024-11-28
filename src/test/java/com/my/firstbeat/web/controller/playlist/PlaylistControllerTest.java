package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.config.security.loginuser.LoginUserService;
import com.my.firstbeat.web.controller.playlist.dto.response.TrackListResponse;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.service.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaylistController.class)
class PlaylistControllerTest extends DummyObject {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaylistService playlistService;
    @MockBean
    private LoginUserService  loginUserService;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        loginUser = new LoginUser(mockUser());

        given(loginUserService.loadUserByUsername(anyString()))
                .willReturn(loginUser);
    }

    @Test
    @WithMockUser(username = "test1234@naver.com")
    @DisplayName("플레이리스트 내 트랙 목록 조회: 정상 케이스")
    void getTrackList_success() throws Exception {
        Long playlistId = 1L;
        int page = 0;
        int size = 10;

        List<Track> tracks = Arrays.asList(
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
        );
        Page<Track> trackPage = new PageImpl<>(tracks);
        TrackListResponse response = new TrackListResponse(trackPage);

        given(playlistService.getTrackList(playlistId, page, size))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/playlist/{playlistId}", playlistId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "test1234@naver.com")
    @DisplayName("플레이리스트 내 트랙 목록 조회: 100을 넘어선 사이즈로 요청을 보내는 경우 유효성 검사 오류")
    void getTrackList_invalid_pageSize() throws Exception {
        mockMvc.perform(get("/api/v1/playlist/1")
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}