package com.my.firstbeat.web.controller.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyPageRequest {
    private Optional<String> name = Optional.empty();
    private Optional<List<String>> favoriteGenre = Optional.empty();
}
