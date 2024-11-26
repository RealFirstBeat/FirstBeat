package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.user.dto.response.GetMyPageResponse;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UserServiceTest extends DummyObject {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGenreRepository userGenreRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("마이페이지 조회 성공 테스트")
    void getUserData_Success() {
        // Given
        User mockUser = mockUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        List<UserGenre> userGenres = mockUserGenres(mockUser);
        when(userGenreRepository.findByUserIdWithGenre(1L)).thenReturn(userGenres);

        // When
        GetMyPageResponse response = userService.getUserData(1L);

        // Then
        assertEquals("test name", response.getName());
        assertEquals("test1234@naver.com", response.getEmail());
        assertEquals(List.of("Rock", "Jazz"), response.getGenres());
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회할 경우 테스트")
    void getUserData_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getUserData(999L));

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }
}
