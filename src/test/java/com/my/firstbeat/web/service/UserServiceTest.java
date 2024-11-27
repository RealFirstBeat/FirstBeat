package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.user.dto.request.UpdateMyPageRequest;
import com.my.firstbeat.web.controller.user.dto.response.GetMyPageResponse;
import com.my.firstbeat.web.controller.user.dto.response.UpdateMyPageResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest extends DummyObject {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGenreRepository userGenreRepository;

    @Mock
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 더미 데이터 초기화
        Genre jazz = Genre.builder().id(1L).name("Jazz").build();

        // 장르 이름을 통해 장르 ID를 조회하도록 설정
        when(genreRepository.findIdsByNames(Set.of("Jazz"))).thenReturn(Set.of(1L));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(jazz));
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


        when(userGenreRepository.findByUserIdWithGenre(1L)).thenReturn(userGenres);

        // Then
        assertEquals("test name", response.getName());
        assertEquals("test1234@naver.com", response.getEmail());
        assertEquals(List.of("Rock", "Pop"), response.getGenres());
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

    @Test
    @DisplayName("마이페이지 수정 성공 테스트")
    void updateMyPage_Success() {
        // Given
        User mockUser = mockUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UpdateMyPageRequest request = new UpdateMyPageRequest();
        request.setName("updatedName");
        request.setFavoriteGenre(Set.of("Jazz", "Rock"));

        List<UserGenre> existingUserGenres = mockUserGenres(mockUser);
        when(userGenreRepository.findByUserIdWithGenre(1L)).thenReturn(existingUserGenres);

        Set<Long> genreIds = Set.of(1L, 2L);

        Genre jazz = Genre.builder().id(1L).name("Jazz").build();
        Genre rock = Genre.builder().id(2L).name("Rock").build();
        when(genreRepository.findAllById(genreIds)).thenReturn(List.of(jazz, rock));

        doAnswer(invocation -> {
            UserGenre userGenre = invocation.getArgument(0);
            existingUserGenres.removeIf(ug -> ug.getGenre().getName().equals("Pop")); // 기존 Pop 삭제
            UserGenre builtUserGenre = UserGenre.builder()
                    .id(userGenre.getGenre().getName().equals("Jazz") ? 1L : 2L)
                    .user(userGenre.getUser())
                    .genre(userGenre.getGenre())
                    .build();
            existingUserGenres.add(builtUserGenre); // 새로운 장르 추가
            return builtUserGenre;
        }).when(userGenreRepository).save(any(UserGenre.class));

        // When
        UpdateMyPageResponse response = userService.updateMyPage(1L, request);

        // Then
        assertEquals("updatedName", response.getName());
        assertEquals(mockUser.getEmail(), response.getEmail());
        assertEquals(Set.of("Jazz", "Rock"), response.getFavoriteGenre());

        // Verify
        verify(userRepository, times(1)).save(mockUser);
        verify(userGenreRepository, times(1)).deleteAll(anyList());
        verify(userGenreRepository, times(2)).findByUserIdWithGenre(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정할 경우 테스트")
    void updateMyPage_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateMyPageRequest request = new UpdateMyPageRequest();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.updateMyPage(999L, request));

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
    }
}
