package com.nodap.application.auth;

import com.nodap.domain.user.entity.Provider;
import com.nodap.domain.user.entity.Role;
import com.nodap.domain.user.entity.User;
import com.nodap.domain.user.entity.UserOauthAccount;
import com.nodap.domain.user.repository.UserOauthAccountRepository;
import com.nodap.domain.user.repository.UserRepository;
import com.nodap.global.error.BusinessException;
import com.nodap.global.error.ErrorCode;
import com.nodap.infrastructure.auth.CookieProvider;
import com.nodap.infrastructure.auth.JwtTokenProvider;
import com.nodap.infrastructure.auth.RefreshTokenRepository;
import com.nodap.infrastructure.external.KakaoOAuthClient;
import com.nodap.infrastructure.external.KakaoOAuthClient.KakaoTokenResponse;
import com.nodap.infrastructure.external.KakaoOAuthClient.KakaoUserInfo;
import com.nodap.interfaces.dto.auth.LoginResponse;
import com.nodap.interfaces.dto.auth.UserSimpleInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 인증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final UserOauthAccountRepository userOauthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieProvider cookieProvider;

    /**
     * 카카오 로그인
     */
    @Transactional
    public LoginResponse loginWithKakao(String authorizationCode, HttpServletResponse response) {
        log.info("[Auth] 카카오 로그인 시작");
        
        // 1. 카카오에서 Access Token 발급
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getAccessToken(authorizationCode);
        
        // 2. 카카오 사용자 정보 조회
        KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(tokenResponse.access_token());

        // 3. 기존 회원 확인 / 탈퇴 후 재가입(복구) / 신규 회원 가입
        Optional<UserOauthAccount> existingOauthAccount = 
                userOauthAccountRepository.findByProviderAndProviderIdWithUser(
                        Provider.KAKAO, userInfo.id());

        User user;
        boolean isNewMember;

        if (existingOauthAccount.isPresent()) {
            // 기존 회원 (미탈퇴)
            user = existingOauthAccount.get().getUser();
            isNewMember = false;
            log.info("[Auth] 기존 회원 로그인 성공: userId={}, kakaoId={}", user.getId(), userInfo.id());
        } else {
            // 탈퇴한 계정 복구 또는 신규 가입
            Optional<UserOauthAccount> deletedAccount = 
                    userOauthAccountRepository.findByProviderAndProviderIdWithUserIncludeDeleted(
                            Provider.KAKAO, userInfo.id());
            if (deletedAccount.isPresent()) {
                UserOauthAccount oa = deletedAccount.get();
                User deletedUser = oa.getUser();
                if (oa.isDeleted() || deletedUser.isDeleted()) {
                    // 탈퇴 후 재가입: 복구 후 회원가입 플로우(isNewMember=true)로 처리
                    oa.restore();
                    deletedUser.restore();
                    userOauthAccountRepository.save(oa);
                    userRepository.save(deletedUser);
                    user = deletedUser;
                    isNewMember = true;
                    log.info("[Auth] 탈퇴 계정 복구 후 재가입: userId={}, kakaoId={}", user.getId(), userInfo.id());
                } else {
                    user = deletedUser;
                    isNewMember = false;
                    log.info("[Auth] 기존 회원 로그인 성공: userId={}, kakaoId={}", user.getId(), userInfo.id());
                }
            } else {
                // 진짜 신규 회원 가입
                user = createNewUser(userInfo);
                isNewMember = true;
                log.info("[Auth] 신규 회원 가입 완료: userId={}, kakaoId={}", user.getId(), userInfo.id());
            }
        }

        // 4. JWT 토큰 발급 및 쿠키 설정
        issueTokensAndSetCookies(user.getId(), response);

        return LoginResponse.of(user, isNewMember);
    }

    /**
     * 토큰 재발급
     */
    @Transactional(readOnly = true)
    public void reissueTokens(HttpServletRequest request, HttpServletResponse response) {
        log.info("[Auth] 토큰 재발급 시작");
        
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractTokenFromCookie(request, CookieProvider.REFRESH_TOKEN_COOKIE);
        
        if (refreshToken == null) {
            log.warn("[Error-AUTH_005] 토큰 재발급 실패 - Refresh Token 없음");
            throw new BusinessException(ErrorCode.AUTH_TOKEN_NOT_FOUND);
        }

        // 2. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("[Error-AUTH_004] 토큰 재발급 실패 - Refresh Token 만료");
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // 3. 토큰 타입 확인
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            log.warn("[Error-AUTH_006] 토큰 재발급 실패 - 유효하지 않은 토큰 타입: {}", tokenType);
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        // 4. 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 5. Redis에 저장된 Refresh Token과 비교
        if (!refreshTokenRepository.validateRefreshToken(userId, refreshToken)) {
            log.warn("[Error-AUTH_004] 토큰 재발급 실패 - Redis 토큰 불일치: userId={}", userId);
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // 6. 새로운 토큰 발급 및 쿠키 설정
        issueTokensAndSetCookies(userId, response);
        
        log.info("[Auth] 토큰 재발급 완료: userId={}", userId);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("[Auth] 로그아웃 시작");
        
        // 1. 쿠키에서 Access Token 추출 (사용자 ID 확인용)
        String accessToken = extractTokenFromCookie(request, CookieProvider.ACCESS_TOKEN_COOKIE);
        
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            
            // 2. Redis에서 Refresh Token 삭제
            refreshTokenRepository.deleteByUserId(userId);
            log.info("[Auth] 로그아웃 완료: userId={}", userId);
        } else {
            log.info("[Auth] 로그아웃 완료 (토큰 없음 또는 만료됨)");
        }

        // 3. 쿠키 삭제
        cookieProvider.deleteCookiesFromResponse(response);
    }

    /**
     * 신규 사용자 생성
     */
    private User createNewUser(KakaoUserInfo userInfo) {
        // 닉네임 생성 (카카오 닉네임 또는 랜덤 닉네임)
        String nickname = generateNickname(userInfo.nickname());
        
        // 이메일 (없으면 기본값)
        String email = userInfo.email() != null ? userInfo.email() : "no-email@nodap.com";

        // User 엔티티 생성
        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .profileImage(userInfo.profileImage())
                .role(Role.USER)
                .build();

        user = userRepository.save(user);
        log.debug("[Auth] 신규 사용자 생성: userId={}, nickname={}", user.getId(), nickname);

        // OAuth 계정 연결
        UserOauthAccount oauthAccount = UserOauthAccount.builder()
                .user(user)
                .provider(Provider.KAKAO)
                .providerId(userInfo.id())
                .build();

        userOauthAccountRepository.save(oauthAccount);
        log.debug("[Auth] OAuth 계정 연결: userId={}, provider=KAKAO", user.getId());

        return user;
    }

    /**
     * 닉네임 생성 (중복 시 랜덤 숫자 추가)
     */
    private String generateNickname(String baseNickname) {
        String nickname = baseNickname != null ? baseNickname : "사용자";
        
        // 닉네임 길이 제한 (최대 10자)
        if (nickname.length() > 10) {
            nickname = nickname.substring(0, 10);
        }

        // 중복 확인 및 처리
        String finalNickname = nickname;
        int attempt = 0;
        while (userRepository.existsByNickname(finalNickname) && attempt < 100) {
            String randomSuffix = String.valueOf((int) (Math.random() * 10000));
            int maxLength = Math.min(nickname.length(), 10 - randomSuffix.length());
            finalNickname = nickname.substring(0, maxLength) + randomSuffix;
            attempt++;
        }

        return finalNickname;
    }

    /**
     * 토큰 발급 및 쿠키 설정
     */
    private void issueTokensAndSetCookies(Long userId, HttpServletResponse response) {
        // Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        
        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        
        // Redis에 Refresh Token 저장
        refreshTokenRepository.save(userId, refreshToken);
        
        // 쿠키에 토큰 설정
        cookieProvider.addCookiesToResponse(response, accessToken, refreshToken);
        
        log.debug("[Auth] 토큰 발급 완료: userId={}", userId);
    }

    /**
     * 개발용 로그인 (local 프로필 전용)
     * userId로 바로 로그인하여 토큰 발급
     */
    @Transactional(readOnly = true)
    public LoginResponse devLogin(Long userId, HttpServletResponse response) {
        log.info("[Auth] 개발용 로그인 시작: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Error-USER_001] 개발용 로그인 실패 - 사용자 없음: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // JWT 토큰 발급 및 쿠키 설정
        issueTokensAndSetCookies(user.getId(), response);
        
        log.info("[Auth] 개발용 로그인 성공: userId={}, nickname={}", user.getId(), user.getNickname());
        
        return LoginResponse.of(user, false);
    }

    /**
     * 전체 사용자 목록 조회 (개발용)
     */
    @Transactional(readOnly = true)
    public List<UserSimpleInfo> getAllUsersForDev() {
        log.debug("[Auth] 전체 사용자 목록 조회 (개발용)");
        return userRepository.findAll().stream()
                .map(UserSimpleInfo::from)
                .toList();
    }

    /**
     * 쿠키에서 토큰 추출
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
