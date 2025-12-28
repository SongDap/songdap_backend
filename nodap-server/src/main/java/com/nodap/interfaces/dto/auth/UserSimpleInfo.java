package com.nodap.interfaces.dto.auth;

import com.nodap.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 간단 정보 DTO (개발용)
 */
@Schema(description = "사용자 간단 정보")
public record UserSimpleInfo(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "닉네임", example = "김개발")
        String nickname,
        @Schema(description = "이메일", example = "dev@example.com")
        String email,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImage
) {
    public static UserSimpleInfo from(User user) {
        return new UserSimpleInfo(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage()
        );
    }
}

