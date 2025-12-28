package com.nodap.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 API 응답 형식
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    /**
     * 에러 응답
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 에러 응답 (ErrorCode 사용)
     */
    public static <T> ApiResponse<T> error(com.nodap.global.error.ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getStatus().value(), errorCode.getMessage(), null);
    }
}



