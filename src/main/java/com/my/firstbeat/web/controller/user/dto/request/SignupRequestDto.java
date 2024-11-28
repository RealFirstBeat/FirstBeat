package com.my.firstbeat.web.controller.user.dto.request;

import com.my.firstbeat.web.controller.user.dto.valid.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {

    @Email
    @NotBlank(message = "이메일을 입력하세요")
    private String email;

    @ValidPassword
    @NotBlank(message = "비밀번호를 입력하세요")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "비밀번호는 영문자와 숫자 조합으로 최소 8글자, 최대 20글자로 입력해주세요."
    )
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 1, message = "이름은 최소 1자 이상 입력해야 합니다.")
    private String name;

    @Size(min = 3, message = "장르를 반드시 3개 이상 선택해주세요.")
    private List<String> genreNames;
}
