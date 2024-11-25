package com.my.firstbeat.web.controller.user.dto.response;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MyPageResponse {
    private String email;
    private String name;
    private List<String> genres;
}
