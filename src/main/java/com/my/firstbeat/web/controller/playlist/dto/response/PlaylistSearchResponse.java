package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaylistSearchResponse {
    private Long playlistId;
    private String name;
    private String description;
    private String createdBy;
}