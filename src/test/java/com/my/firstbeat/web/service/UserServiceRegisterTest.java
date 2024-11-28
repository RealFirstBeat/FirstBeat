package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.user.dto.request.SignupRequestDto;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.domain.userGenre.UserGenre;
import com.my.firstbeat.web.domain.userGenre.UserGenreRepository;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceRegisterTest extends DummyObject {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGenreRepository userGenreRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    @DisplayName("유저 회원가입 성공 테스트")
    void testSignup_Success() {
        // Given
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("Test1234")
                .name("Test User")
                .genreNames(List.of("Rock", "Jazz", "Pop"))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test1234")).thenReturn("encodedPassword");
        when(genreRepository.findByName("Rock")).thenReturn(Optional.of(new Genre(1L, "Rock")));
        when(genreRepository.findByName("Jazz")).thenReturn(Optional.of(new Genre(2L, "Jazz")));
        when(genreRepository.findByName("Pop")).thenReturn(Optional.of(new Genre(3L, "Pop")));

        // When
        String result = userService.signup(signupRequestDto);

        // Then
        assertThat(result).isEqualTo("회원가입이 정상적으로 처리되었습니다.");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userGenreRepository, times(3)).save(any(UserGenre.class));
    }

    @Test
    @DisplayName("회원가입시 이메일 중복 테스트")
    void testSignup_DuplicateEmail() {
        // Given
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .email("test1234@naver.com")
                .password("Test1234")
                .name("test name")
                .genreNames(List.of("Rock", "Jazz", "Pop"))
                .build();

        when(userRepository.existsByEmail("test1234@naver.com")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.signup(signupRequestDto)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("장르 유효성 테스트")
    void testSignup_InvalidGenres() {
        // Given
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("Test1234")
                .name("Test User")
                .genreNames(List.of("Rock", "Jazz", "Pop"))
                .build(); // Only one genre

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.signup(signupRequestDto)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GENRES);
        verify(userRepository, never()).save(any(User.class));
    }
}
