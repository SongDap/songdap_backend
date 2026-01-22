package com.nodap.interfaces.dto.user;

import com.nodap.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 정보 응답 DTO
 */
@Schema(description = "사용자 정보")
public record UserInfoResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "김개발")
        String nickname,

        @Schema(description = "이메일", example = "dev@example.com")
        String email,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImage
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage()
        );
    }
}
