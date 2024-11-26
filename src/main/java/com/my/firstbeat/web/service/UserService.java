package com.my.firstbeat.web.service;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.controller.user.dto.request.UpdateMyPageRequest;
import com.my.firstbeat.web.controller.user.dto.response.GetMyPageResponse;
import com.my.firstbeat.web.controller.user.dto.response.UpdateMyPageResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.genre.GenreRepository;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.controller.user.dto.response.MyPageResponse;
import com.my.firstbeat.web.domain.userGenre.UserGenre;
import com.my.firstbeat.web.domain.userGenre.UserGenreRepository;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final GenreRepository genreRepository; // GenreRepository 추가


    public User findByIdOrFail(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 매개변수로 입력받은 userId에 해당하는 유저명과 이메일, 관심장르를 MyPageResponse 객체에 담아 반환
     * @param userId 유저명, 이메일, 관심장르를 반환할 유저의 Id
     * @return MyPageResponse
     * @exception BusinessException NotFoundUserException
     */
    public GetMyPageResponse getUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserGenre> userGenres = userGenreRepository.findByUserIdWithGenre(userId);
        // 관심 장르 조회
        List<String> genres = userGenres.stream()
                .map(userGenre -> userGenre.getGenre().getName())
                .collect(Collectors.toList());

        log.info("유저명: {}, 이메일: {}, 관심장르: {}", user.getName(), user.getEmail(), genres);
        return new GetMyPageResponse(user.getName(), user.getEmail(), genres);
    }

    /**
     * 매개변수로 입력받은 userId에 해당하는 유저 정보를 반환한 후 닉네임과 관심장르를 수정
     * @param userId 유저 정보를 반환할 유저의 Id
     * @param request {"name": "test", "favoriteGenre": {"genre1", "genre2", "genre3"}}
     * @return UpdateMyPageResponse
     * @exception BusinessException USER_NOT_FOUND
     */
    @Transactional
    public UpdateMyPageResponse updateMyPage(Long userId, UpdateMyPageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이름 업데이트
        request.getName().ifPresent(user::setName);
        userRepository.save(user);

        // 관심 장르 업데이트
        request.getFavoriteGenre().ifPresent(favoriteGenres -> {
            // 기존 장르 삭제
            userGenreRepository.deleteByUserId(userId);

            // 새로운 장르 추가
            List<Genre> genres = genreRepository.findByNameIn(favoriteGenres);
            for (Genre genre : genres) {
                UserGenre userGenre = UserGenre.builder()
                        .user(user)
                        .genre(genre)
                        .build();
                userGenreRepository.save(userGenre);
            }
        });

        // 업데이트된 장르 조회
        List<UserGenre> updatedUserGenres = userGenreRepository.findByUserIdWithGenre(userId);
        Set<String> updatedGenres = updatedUserGenres.stream()
                .map(userGenre -> userGenre.getGenre().getName())
                .collect(Collectors.toSet());

        log.info("유저명: {}, 관심 장르: {}", user.getName(), updatedGenres);

        return new UpdateMyPageResponse(user.getName(), user.getEmail(), updatedGenres);
    }
}
