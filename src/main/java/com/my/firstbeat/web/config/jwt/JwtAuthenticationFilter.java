package com.my.firstbeat.web.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.firstbeat.web.config.security.handler.SecurityResponseHandler;
import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import com.my.firstbeat.web.config.security.loginuser.dto.LoginRequest;
import com.my.firstbeat.web.config.security.loginuser.dto.LoginResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper om;
    private final SecurityResponseHandler securityResponseHandler;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ObjectMapper om, SecurityResponseHandler securityResponseHandler) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/v1/auth/login");

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.om = om;
        this.securityResponseHandler = securityResponseHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            LoginRequest loginReqDto = om.readValue(request.getInputStream(), LoginRequest.class);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginReqDto.getEmail(),
                            loginReqDto.getPassword());
            return authenticationManager.authenticate(authenticationToken);
        }catch (Exception e){
            log.info(e.getMessage(), e);
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String token = jwtUtil.createToken(loginUser.getUser());
        LoginResponse loginRespDto = new LoginResponse(loginUser.getUser());

        response.addHeader(JwtVo.HEADER, token);
        securityResponseHandler.success(response, loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        securityResponseHandler.fail(response, "로그인에 실패했습니다", HttpStatus.UNAUTHORIZED);
    }
}
