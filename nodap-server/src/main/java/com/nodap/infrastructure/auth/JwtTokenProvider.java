package com.nodap.infrastructure.auth;

import com.nodap.global.error.BusinessException;
import com.nodap.global.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId) {
        return createToken(userId, jwtProperties.accessTokenExpiry(), "ACCESS");
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        return createToken(userId, jwtProperties.refreshTokenExpiry(), "REFRESH");
    }

    /**
     * 토큰 생성 공통 로직
     */
    private String createToken(Long userId, long expiryTime, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryTime);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰 타입 확인 (ACCESS / REFRESH)
     */
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[Error-AUTH_004] 만료된 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("[Error-AUTH_006] 유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.error("[Error-AUTH_006] 토큰 파싱 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Access Token 만료 시간 반환 (초)
     */
    public long getAccessTokenExpirySeconds() {
        return jwtProperties.accessTokenExpiry() / 1000;
    }

    /**
     * Refresh Token 만료 시간 반환 (초)
     */
    public long getRefreshTokenExpirySeconds() {
        return jwtProperties.refreshTokenExpiry() / 1000;
    }
}
