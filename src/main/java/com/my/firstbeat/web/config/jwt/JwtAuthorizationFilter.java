package com.my.firstbeat.web.config.jwt;

import com.my.firstbeat.web.config.security.loginuser.LoginUser;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private static final String AUTH_PATH = "auth";

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,  JwtUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{
            if(isPublicPath(request)){
                chain.doFilter(request, response);
                return;
            }

            if(request.getRequestURI().contains("tracks")){
                log.info("추천 트랙 반환 경로라서 패스함~");
                chain.doFilter(request, response);
                return;
            }

            if(request.getRequestURI().contains("metrics")){
                chain.doFilter(request, response);
                return;
            }



            if(!isHeaderValid(request)){
                throw new JwtException("Authorization 헤더가 누락되었습니다");
            }



            //토큰 파싱
            String token = jwtUtil.substringToken(request.getHeader(JwtVo.HEADER));

            //토큰 검증
            LoginUser loginUser = jwtUtil.validateToken(token);

            //인증된 유저 컨텍스트에 설정
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("유저 인증 완료 - userId : {}, role : {}", loginUser.getUser().getId(), loginUser.getUser().getRole());

            chain.doFilter(request, response);

        } catch(JwtException e){
            throw e;
        } catch(Exception e){
            log.error("인가 중 서버 및 내부 오류 발생: ",e.getMessage(), e);
            throw e;
        }
    }

    private boolean isHeaderValid(HttpServletRequest request) {
        String header = request.getHeader(JwtVo.HEADER);
        return header != null && header.startsWith(JwtVo.TOKEN_PREFIX);
    }

    private boolean isPublicPath(HttpServletRequest request){
        return request.getRequestURI().contains(AUTH_PATH);
    }
}
