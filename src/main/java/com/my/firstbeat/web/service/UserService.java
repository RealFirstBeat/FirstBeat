package com.my.firstbeat.web.service;

import com.my.firstbeat.web.controller.user.dto.response.MyPageResponse;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.domain.userGenre.UserGenre;
import com.my.firstbeat.web.domain.userGenre.UserGenreRepository;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;

    /**
     * 매개변수로 입력받은 userId에 해당하는 유저명과 이메일, 관심장르를 MyPageResponse 객체에 담아 반환
     * @param userId 유저명, 이메일, 관심장르를 반환할 유저의 Id
     * @return MyPageResponse
     * @exception BusinessException NotFoundUserException
     */
    public MyPageResponse getUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NotFoundUser));

        List<UserGenre> userGenres = userGenreRepository.findByUserId(userId);
        List<String> genres = userGenres.stream()
                .map(userGenre -> userGenre.getGenre().getName())
                .collect(Collectors.toList());

        return new MyPageResponse(user.getName(), user.getEmail(), genres);
    }
}
