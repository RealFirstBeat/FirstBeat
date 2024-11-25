package com.my.firstbeat.web.controller.playlist.dto.request;

import com.my.firstbeat.web.domain.user.User;
import lombok.Getter;

@Getter
public class PlaylistCreateRequest {

    private String Title;
    private String Description;
    private User user;

    public PlaylistCreateRequest(String Title, String Description) {
        this.Title = Title;
        this.Description = Description;
    }

    public PlaylistCreateRequest(String Title, String Description, User user) {
        this.Title = Title;
        this.Description = Description;
        this.user = user;
    }
}
