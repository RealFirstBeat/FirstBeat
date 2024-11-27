package com.my.firstbeat.web.handler;

import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.util.api.ApiError;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<ApiError>> handleGeneralException(Exception e) {
        log.error("예기치 못한 오류 발생: {}", e.getMessage(), e);
        return new ResponseEntity<>(ApiResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 에러가 발생했습니다. 잠시 후 다시 시도해주세요"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<ApiError>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String errorMsg = String.format("잘못된 HTTP 메서드를 사용했습니다. 가능한 HTTP 메서드: %s", e.getSupportedHttpMethods());
        return new ResponseEntity<>(ApiResult.error(HttpStatus.METHOD_NOT_ALLOWED.value(), errorMsg), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> validationException(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errorMap.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(ApiResult.error(HttpStatus.BAD_REQUEST.value(), "유효성 검사 실패", errorMap), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResult<ApiError>> handleNoResourceFoundException(NoResourceFoundException e) {
        return new ResponseEntity<>(ApiResult.error(HttpStatus.NOT_FOUND.value(), "요청된 URI를 찾을 수 없습니다"), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<ApiError>> apiException(BusinessException e) {
        return new ResponseEntity<>(ApiResult.error(e.getErrorCode().getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getErrorCode().getStatus()));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> queryParameterValidationException(ConstraintViolationException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getConstraintViolations().forEach(error -> {
            String propertyPath = error.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errorMap.put(fieldName, error.getMessage());
        });
        return new ResponseEntity<>(ApiResult.error(HttpStatus.BAD_REQUEST.value(), "유효성 검사 실패", errorMap), HttpStatus.BAD_REQUEST);
    }

}
