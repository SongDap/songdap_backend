package com.nodap.application.user;

import com.nodap.domain.user.entity.User;
import com.nodap.domain.user.entity.UserOauthAccount;
import com.nodap.domain.user.repository.UserOauthAccountRepository;
import com.nodap.domain.user.repository.UserRepository;
import com.nodap.global.error.BusinessException;
import com.nodap.global.error.ErrorCode;
import com.nodap.infrastructure.auth.CookieProvider;
import com.nodap.infrastructure.auth.RefreshTokenRepository;
import com.nodap.interfaces.dto.user.CheckNicknameResponse;
import com.nodap.interfaces.dto.user.UpdateUserRequest;
import com.nodap.interfaces.dto.user.UserInfoResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 사용자 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserOauthAccountRepository userOauthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieProvider cookieProvider;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        log.info("[User] 사용자 정보 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Error-USER_001] 사용자 정보 조회 실패 - 사용자 없음: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        return UserInfoResponse.from(user);
    }

    /**
     * 현재 로그인한 사용자 정보 수정
     */
    @Transactional
    public UserInfoResponse updateMyInfo(Long userId, UpdateUserRequest request) {
        log.info("[User] 사용자 정보 수정: userId={}, nickname={}, email={}", 
                userId, request.nickname(), request.email());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Error-USER_001] 사용자 정보 수정 실패 - 사용자 없음: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 닉네임 중복 확인 (본인 닉네임 제외)
        if (!user.getNickname().equals(request.nickname()) && 
            userRepository.existsByNickname(request.nickname())) {
            log.warn("[Error-USER_002] 닉네임 중복: nickname={}", request.nickname());
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 사용자 정보 업데이트
        user.updateNickname(request.nickname());
        
        if (request.email() != null && !request.email().isBlank()) {
            user.updateEmail(request.email());
        }
        
        if (request.profileImage() != null && !request.profileImage().isBlank()) {
            user.updateProfileImage(request.profileImage());
        }

        userRepository.save(user);
        log.info("[User] 사용자 정보 수정 완료: userId={}", userId);

        return UserInfoResponse.from(user);
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public CheckNicknameResponse checkNickname(String nickname) {
        log.info("[User] 닉네임 중복 확인: nickname={}", nickname);

        if (!StringUtils.hasText(nickname)) {
            log.warn("[Error-ERR_001] 닉네임 파라미터 누락");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        boolean isDuplicated = userRepository.existsByNickname(nickname);
        log.info("[User] 닉네임 중복 확인 결과: nickname={}, isDuplicated={}", nickname, isDuplicated);

        return new CheckNicknameResponse(isDuplicated);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long userId, HttpServletResponse response) {
        log.info("[User] 회원 탈퇴 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Error-USER_001] 회원 탈퇴 실패 - 사용자 없음: userId={}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 1. Redis에서 Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(userId);
        log.debug("[User] Refresh Token 삭제 완료: userId={}", userId);

        // 2. OAuth 계정 삭제 (카카오 연동 해제)
        UserOauthAccount oauthAccount = user.getOauthAccount();
        if (oauthAccount != null) {
            oauthAccount.delete();
            userOauthAccountRepository.save(oauthAccount);
            log.debug("[User] OAuth 계정 삭제 완료: userId={}", userId);
        }

        // 3. 사용자 정보 Soft Delete
        user.delete();
        userRepository.save(user);
        log.info("[User] 회원 탈퇴 완료: userId={}", userId);

        // 4. 쿠키 삭제
        cookieProvider.deleteCookiesFromResponse(response);
        log.debug("[User] 쿠키 삭제 완료: userId={}", userId);
    }
}
