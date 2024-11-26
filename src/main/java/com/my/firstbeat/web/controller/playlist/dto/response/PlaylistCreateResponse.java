package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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
