package com.my.firstbeat.web.service;

import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User findByIdOrFail(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUNT));
    }
}
