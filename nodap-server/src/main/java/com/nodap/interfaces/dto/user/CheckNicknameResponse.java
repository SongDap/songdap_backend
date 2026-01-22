package com.nodap.interfaces.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 닉네임 중복 확인 응답 DTO
 */
@Schema(description = "닉네임 중복 확인 응답")
public record CheckNicknameResponse(
        @Schema(description = "중복 여부 (true: 중복됨/사용 불가, false: 사용 가능)", example = "false")
        boolean isDuplicated
) {
}
