package com.my.firstbeat.web.controller.playlist.dto.request;

import lombok.Getter;

@Getter
public class PlaylistCreateRequest {

    private String Title;
    private String Description;

    public PlaylistCreateRequest(String Title, String Description) {
        this.Title = Title;
        this.Description = Description;
    }
}
