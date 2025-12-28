package com.nodap.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개발용 로그인 요청 DTO
 * local 프로필에서만 사용 가능
 */
@Schema(description = "개발용 로그인 요청 (local 환경 전용)")
@Getter
@NoArgsConstructor
public class DevLoginRequest {

    @Schema(
            description = "로그인할 사용자 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Positive(message = "유효한 사용자 ID가 필요합니다.")
    private Long userId;

    public DevLoginRequest(Long userId) {
        this.userId = userId;
    }
}

