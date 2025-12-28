package com.nodap.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 요청 DTO
 */
@Schema(description = "카카오 로그인 요청")
@Getter
@NoArgsConstructor
public class KakaoLoginRequest {

    @Schema(
            description = "카카오 인가 코드",
            example = "authorization_code_from_kakao",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "인가 코드가 필요합니다.")
    private String authorizationCode;

    public KakaoLoginRequest(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}
