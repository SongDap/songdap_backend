package com.nodap.interfaces.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 사용자 정보 수정 요청 DTO
 */
@Schema(description = "사용자 정보 수정 요청")
public record UpdateUserRequest(
        @Schema(description = "닉네임 (필수)", example = "김개발")
        @Size(min = 1, max = 16, message = "닉네임은 1자 이상 16자 이하여야 합니다.")
        String nickname,

        @Schema(description = "이메일 (선택)", example = "dev@example.com")
        @Size(max = 128, message = "이메일은 128자 이하여야 합니다.")
        String email,

        @Schema(description = "프로필 이미지 URL (선택)", example = "https://example.com/profile.jpg")
        @Size(max = 256, message = "프로필 이미지 URL은 256자 이하여야 합니다.")
        String profileImage
) {
}
