package com.nodap.infrastructure.external;

import com.nodap.global.error.BusinessException;
import com.nodap.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * 카카오 OAuth API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final KakaoOAuthProperties kakaoProperties;
    private final WebClient webClient = WebClient.builder().build();

    /**
     * 인가 코드로 Access Token 발급
     */
    public KakaoTokenResponse getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoProperties.clientId());
        formData.add("redirect_uri", kakaoProperties.redirectUri());
        formData.add("code", authorizationCode);
        
        if (kakaoProperties.clientSecret() != null && !kakaoProperties.clientSecret().isEmpty()) {
            formData.add("client_secret", kakaoProperties.clientSecret());
        }

        try {
            log.debug("[Kakao] Access Token 발급 요청");
            return webClient.post()
                    .uri(kakaoProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[Error-AUTH_002] 카카오 토큰 발급 실패: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.AUTH_CODE_EXPIRED);
        } catch (Exception e) {
            log.error("[Error-KAKAO_ERR] 카카오 API 호출 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     */
    @SuppressWarnings("unchecked")
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            log.debug("[Kakao] 사용자 정보 조회 요청");
            Map<String, Object> response = webClient.get()
                    .uri(kakaoProperties.userInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                log.error("[Error-KAKAO_ERR] 카카오 사용자 정보 응답이 null");
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
            }

            // 카카오 응답 파싱
            String id = String.valueOf(response.get("id"));
            
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null 
                    ? (Map<String, Object>) kakaoAccount.get("profile") 
                    : null;

            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

            log.debug("[Kakao] 사용자 정보 조회 성공: kakaoId={}", id);
            return new KakaoUserInfo(id, email, nickname, profileImage);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Error-KAKAO_ERR] 카카오 사용자 정보 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * 카카오 연동 해제
     * @param targetId 카카오 사용자 ID (providerId)
     */
    public void unlink(String targetId) {
        try {
            log.debug("[Kakao] 카카오 연동 해제 요청: targetId={}", targetId);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("target_id_type", "user_id");
            formData.add("target_id", targetId);
            
            // Admin Key를 사용하여 연동 해제 (KakaoAK 방식)
            webClient.post()
                    .uri(kakaoProperties.unlinkUri())
                    .header("Authorization", "KakaoAK " + kakaoProperties.clientId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.info("[Kakao] 카카오 연동 해제 성공: targetId={}", targetId);
        } catch (WebClientResponseException e) {
            log.error("[Error-KAKAO_ERR] 카카오 연동 해제 실패: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            // 연동 해제 실패해도 회원탈퇴는 진행 (이미 DB에서 삭제 처리)
            log.warn("[Kakao] 카카오 연동 해제 실패했지만 회원탈퇴는 계속 진행: targetId={}", targetId);
        } catch (Exception e) {
            log.error("[Error-KAKAO_ERR] 카카오 연동 해제 API 호출 실패: {}", e.getMessage());
            // 연동 해제 실패해도 회원탈퇴는 진행 (이미 DB에서 삭제 처리)
            log.warn("[Kakao] 카카오 연동 해제 실패했지만 회원탈퇴는 계속 진행: targetId={}", targetId);
        }
    }

    /**
     * 카카오 토큰 응답 DTO
     */
    public record KakaoTokenResponse(
            String access_token,
            String token_type,
            String refresh_token,
            Integer expires_in,
            String scope,
            Integer refresh_token_expires_in
    ) {}

    /**
     * 카카오 사용자 정보 DTO
     */
    public record KakaoUserInfo(
            String id,
            String email,
            String nickname,
            String profileImage
    ) {}
}
