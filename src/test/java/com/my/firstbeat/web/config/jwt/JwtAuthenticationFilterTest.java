package com.my.firstbeat.web.config.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.firstbeat.web.config.security.loginuser.dto.LoginRequest;
import com.my.firstbeat.web.config.security.loginuser.dto.LoginResponse;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.user.UserRepository;
import com.my.firstbeat.web.dummy.DummyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFilterTest extends DummyObject {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        user = userRepository.save(mockUser());
    }

    @Test
    @DisplayName("인증 성공 테스트")
    void success_authentication_test() throws Exception {
        LoginRequest request = new LoginRequest(user.getEmail(), mockUserPassword);
        String requestBody = om.writeValueAsString(request);

        ResultActions resultActions = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value(LoginResponse.SUCCESS));

        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String token = resultActions.andReturn().getResponse().getHeader(JwtVo.HEADER);

        System.out.println("responseBody = " + responseBody);
        System.out.println("token = " + token);
        assertTrue(token.startsWith(JwtVo.TOKEN_PREFIX));

    }


    @Test
    @DisplayName("인증 실패 테스트")
    void fail_authentication_test() throws Exception {
        LoginRequest request = new LoginRequest(user.getEmail()+"123", mockUserPassword);
        String requestBody = om.writeValueAsString(request);

        ResultActions resultActions = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());

        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody = " + responseBody);

    }



}