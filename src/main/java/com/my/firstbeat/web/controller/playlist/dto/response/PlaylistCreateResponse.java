package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.Getter;

@Getter
public class PlaylistCreateResponse {

    private Long id;
    private String title;
    private String description;

    public PlaylistCreateResponse(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

}
