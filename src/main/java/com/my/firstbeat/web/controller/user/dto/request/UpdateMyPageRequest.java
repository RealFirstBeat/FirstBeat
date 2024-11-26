package com.my.firstbeat.web.controller.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyPageRequest {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotEmpty(message = "Favorite genres cannot be empty")
    private List<String> favoriteGenre;
}
