package com.nodap.interfaces.dto.auth;

import com.nodap.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 응답 DTO
 */
@Schema(description = "로그인 응답")
@Getter
@Builder
public class LoginResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "김개발")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "http://k.kakaocdn.net/...")
    private String profileImage;

    @Schema(description = "신규 회원 여부", example = "false")
    private boolean isNewMember;

    public static LoginResponse of(User user, boolean isNewMember) {
        return LoginResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .isNewMember(isNewMember)
                .build();
    }
}
