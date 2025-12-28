package com.nodap.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 형식
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int code;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    /**
     * 기본 에러 응답
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                null
        );
    }

    /**
     * 커스텀 메시지 에러 응답
     */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * 필드 에러 포함 응답 (Validation 에러용)
     */
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                errors
        );
    }

    /**
     * 필드 에러 DTO
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
    }
}



