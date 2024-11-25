package com.my.firstbeat.web.controller.playlist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaylistCreateRequest {

    @NotEmpty(message = "Title은 비어있을 수 없습니다.")
    @Size(max = 30, message = "30자 내로 작성해주세요.")
    private String title;

    @Size(max = 256, message = "256자 내외로 작성해주세요.")
    private String description;

    public PlaylistCreateRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
