package com.nodap.interfaces.controller;

import com.nodap.application.auth.AuthService;
import com.nodap.global.common.ApiResponse;
import com.nodap.interfaces.dto.auth.DevLoginRequest;
import com.nodap.interfaces.dto.auth.LoginResponse;
import com.nodap.interfaces.dto.auth.UserSimpleInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 개발용 인증 API 컨트롤러
 * local 프로필에서만 활성화됨
 */
@Tag(name = "Dev Auth", description = "개발용 인증 API (local 환경 전용)")
@Profile("local")
@RestController
@RequestMapping("/api/v1/dev/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final AuthService authService;

    /**
     * 개발용 로그인
     */
    @Operation(summary = "개발용 간편 로그인", description = """
            카카오 인증 없이 userId로 바로 로그인합니다.

            - local 환경에서만 사용 가능합니다.
            - 기존 사용자의 userId를 입력하면 바로 토큰이 발급됩니다.
            - Access Token과 Refresh Token이 쿠키로 설정됩니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_001: 사용자를 찾을 수 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> devLogin(
            @Valid @RequestBody DevLoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.devLogin(request.getUserId(), response);

        return ResponseEntity.ok(ApiResponse.success("개발용 로그인 성공", loginResponse));
    }

    /**
     * 전체 사용자 목록 조회 (개발용)
     */
    @Operation(summary = "전체 사용자 목록 조회", description = """
            등록된 모든 사용자 목록을 조회합니다.

            - local 환경에서만 사용 가능합니다.
            - 개발용 로그인에 필요한 userId를 확인하는 용도입니다.
            - 사용자의 기본 정보(userId, nickname, email, profileImage)를 반환합니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공", content = @Content(schema = @Schema(implementation = UserSimpleInfo.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSimpleInfo>>> getAllUsers() {
        List<UserSimpleInfo> users = authService.getAllUsersForDev();

        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", users));
    }
}
