package com.my.firstbeat.web.service;

import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

@SpringBootTest
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    @Rollback(value = false)
    void test(){

        User user = userRepository.findById(2L).get();
        user.setName("마에다");
        String password = "Riku1234";
        String encode = passwordEncoder.encode(password);
        user.setPassword(encode);
        user.setEmail("riku1234@naver.com");

        userRepository.save(user);
    }
}
