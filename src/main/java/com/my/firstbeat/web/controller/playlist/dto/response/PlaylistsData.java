package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlaylistsData {

    private final List<PlaylistSearchResponse> playlists;
    private final PaginationInfo pagination;

}
