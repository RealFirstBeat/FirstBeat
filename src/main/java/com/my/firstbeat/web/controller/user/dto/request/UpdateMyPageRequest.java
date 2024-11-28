package com.my.firstbeat.web.controller.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyPageRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 2147483647, message = "Name must be at least 1 character")
    private String name;

    @NotEmpty(message = "Favorite genres cannot be empty")
    private Set<String> favoriteGenre;
}
