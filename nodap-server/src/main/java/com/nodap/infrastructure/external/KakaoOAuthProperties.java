package com.nodap.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오 OAuth 설정 프로퍼티
 * 생성자 바인딩 사용 (Setter 금지)
 */
@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        /**
         * 카카오 앱 REST API 키
         */
        String clientId,

        /**
         * 카카오 앱 Secret 키
         */
        String clientSecret,

        /**
         * 인가 코드 리다이렉트 URI
         */
        String redirectUri,

        /**
         * 토큰 발급 URI
         */
        String tokenUri,

        /**
         * 사용자 정보 조회 URI
         */
        String userInfoUri,

        /**
         * 카카오 연동 해제 URI
         */
        String unlinkUri
) {
    public KakaoOAuthProperties {
        if (tokenUri == null) {
            tokenUri = "https://kauth.kakao.com/oauth/token";
        }
        if (userInfoUri == null) {
            userInfoUri = "https://kapi.kakao.com/v2/user/me";
        }
        if (unlinkUri == null) {
            unlinkUri = "https://kapi.kakao.com/v1/user/unlink";
        }
    }
}
