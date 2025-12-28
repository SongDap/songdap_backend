package com.nodap.infrastructure.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 쿠키 생성 및 관리
 */
@Component
@RequiredArgsConstructor
public class CookieProvider {

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Access Token 쿠키 생성
     */
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(
                ACCESS_TOKEN_COOKIE,
                accessToken,
                jwtTokenProvider.getAccessTokenExpirySeconds()
        );
    }

    /**
     * Refresh Token 쿠키 생성
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirySeconds()
        );
    }

    /**
     * 쿠키 삭제 (로그아웃 시 사용)
     */
    public ResponseCookie createExpiredCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }

    /**
     * 쿠키 생성 공통 로직
     */
    private ResponseCookie createCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * Response에 쿠키 추가
     */
    public void addCookiesToResponse(HttpServletResponse response, 
                                      String accessToken, 
                                      String refreshToken) {
        ResponseCookie accessCookie = createAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = createRefreshTokenCookie(refreshToken);
        
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    /**
     * Response에서 쿠키 삭제 (로그아웃)
     */
    public void deleteCookiesFromResponse(HttpServletResponse response) {
        ResponseCookie expiredAccessCookie = createExpiredCookie(ACCESS_TOKEN_COOKIE);
        ResponseCookie expiredRefreshCookie = createExpiredCookie(REFRESH_TOKEN_COOKIE);
        
        response.addHeader("Set-Cookie", expiredAccessCookie.toString());
        response.addHeader("Set-Cookie", expiredRefreshCookie.toString());
    }
}



