package com.my.firstbeat.web.util.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "data", "apiError"})
public class ApiResult<T> {

    private final ApiError error;
    private final boolean success;
    private final T data;

    public ApiResult(boolean success, T data, ApiError error) {
        this.error = error;
        this.success = success;
        this.data = data;
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> error(int status, String msg) {
        return new ApiResult<>(false, null, new ApiError(msg, status));
    }

    public static <T> ApiResult<T> error(int status, String msg, T errorMap) {
        return new ApiResult<>(false, errorMap, new ApiError(msg, status));
    }

}
/*
 *
 * success
 * time
 * data
 * */
