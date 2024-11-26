package com.my.firstbeat.web.controller.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyPageResponse {
    private String name;
    private String email;
    private List<String> genres;
}
