package com.nodap.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ============================================
    // 공통 에러 (400)
    // ============================================
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "ERR_001", "잘못된 요청입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "ERR_002", "잘못된 타입입니다."),
    
    // ============================================
    // 인증 에러 (401)
    // ============================================
    AUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "AUTH_001", "인가 코드가 없습니다."),
    AUTH_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 인가 코드입니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_003", "인증이 필요합니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "리프레시 토큰이 만료되었습니다."),
    AUTH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_005", "토큰이 존재하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 토큰입니다."),

    // ============================================
    // 권한 에러 (403)
    // ============================================
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_010", "접근 권한이 없습니다."),

    // ============================================
    // 리소스 에러 (404)
    // ============================================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALBUM_001", "앨범을 찾을 수 없습니다."),
    MUSIC_NOT_FOUND(HttpStatus.NOT_FOUND, "MUSIC_001", "수록곡을 찾을 수 없습니다."),

    // ============================================
    // 비즈니스 에러 (409)
    // ============================================
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
    MUSIC_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "ALBUM_002", "수록곡 제한을 초과했습니다."),

    // ============================================
    // 외부 API 에러 (502)
    // ============================================
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO_ERR", "카카오 서버 오류입니다."),
    
    // ============================================
    // 서버 에러 (500)
    // ============================================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_ERR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}



