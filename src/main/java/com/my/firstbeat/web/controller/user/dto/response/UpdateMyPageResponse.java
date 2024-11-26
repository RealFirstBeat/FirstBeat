package com.my.firstbeat.web.controller.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UpdateMyPageResponse {
    private String name;
    private String email;
    private Set<String> favoriteGenre;
}
