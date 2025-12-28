package com.nodap.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Refresh Token Redis 저장소
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";
    
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * Refresh Token 저장
     */
    public void save(Long userId, String refreshToken) {
        String key = KEY_PREFIX + userId;
        Duration expiry = Duration.ofMillis(jwtProperties.refreshTokenExpiry());
        redisTemplate.opsForValue().set(key, refreshToken, expiry);
        log.debug("[RefreshToken] 토큰 저장 완료: userId={}", userId);
    }

    /**
     * Refresh Token 조회
     */
    public Optional<String> findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    public void deleteByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("[RefreshToken] 토큰 삭제 완료: userId={}", userId);
    }

    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        return findByUserId(userId)
                .map(storedToken -> storedToken.equals(refreshToken))
                .orElse(false);
    }
}
