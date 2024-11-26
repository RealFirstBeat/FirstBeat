package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaylistRetrieveResponse {

    private Long id;
    private String title;

    public PlaylistRetrieveResponse(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}
