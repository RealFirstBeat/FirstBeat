package com.my.firstbeat.web.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.firstbeat.web.util.api.ApiError;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.rmi.ServerException;

@Slf4j
@RequiredArgsConstructor
public class SecurityResponseHandler {

    private final ObjectMapper om;

    public void fail(HttpServletResponse response, String msg, HttpStatus httpStatus){
        try {
            ApiResult<ApiError> error = ApiResult.error(httpStatus.value(), msg);
            String responseBody = om.writeValueAsString(error);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(httpStatus.value());
            response.getWriter().write(responseBody);
            response.getWriter().flush();
        }catch (Exception e){
            log.error("시큐리티 예외 응답 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void success(HttpServletResponse response, Object obj){
        try{
            String responseBody = om.writeValueAsString(ApiResult.success(obj));
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(responseBody);
            response.getWriter().flush();
        }catch (Exception e){
            log.error("시큐리티 성공 응답 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
