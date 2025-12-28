package com.nodap.infrastructure.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 프로퍼티
 * 생성자 바인딩 사용 (Setter 금지)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        /**
         * JWT 서명에 사용되는 비밀 키
         */
        String secret,

        /**
         * Access Token 만료 시간 (밀리초)
         * 기본값: 30분
         */
        Long accessTokenExpiry,

        /**
         * Refresh Token 만료 시간 (밀리초)
         * 기본값: 7일
         */
        Long refreshTokenExpiry
) {
    public JwtProperties {
        if (accessTokenExpiry == null) {
            accessTokenExpiry = 1800000L; // 30분
        }
        if (refreshTokenExpiry == null) {
            refreshTokenExpiry = 604800000L; // 7일
        }
    }
}
