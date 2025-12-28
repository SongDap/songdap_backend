package com.nodap.interfaces.controller;

import com.nodap.application.auth.AuthService;
import com.nodap.global.common.ApiResponse;
import com.nodap.interfaces.dto.auth.KakaoLoginRequest;
import com.nodap.interfaces.dto.auth.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 */
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        /**
         * 카카오 로그인
         */
        @Operation(summary = "카카오 로그인", description = """
                        카카오 인가 코드를 이용한 로그인/회원가입을 수행합니다.

                        - 신규 회원인 경우 자동으로 회원가입이 진행됩니다.
                        - 로그인 성공 시 Access Token과 Refresh Token이 쿠키로 설정됩니다.
                        - 쿠키 속성: HttpOnly, Secure, SameSite=None
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_001: 인가 코드가 없습니다."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH_002: 유효하지 않은 인가 코드 (만료 또는 이미 사용됨)"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "KAKAO_ERR: 카카오 서버 오류")
        })
        @PostMapping("/login/kakao")
        public ResponseEntity<ApiResponse<LoginResponse>> loginWithKakao(
                        @Valid @RequestBody KakaoLoginRequest request,
                        HttpServletResponse response) {

                LoginResponse loginResponse = authService.loginWithKakao(
                                request.getAuthorizationCode(), response);

                return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));
        }

        /**
         * 토큰 재발급
         */
        @Operation(summary = "토큰 재발급", description = """
                        쿠키에 저장된 Refresh Token을 사용하여 새로운 토큰을 발급받습니다.

                        - Access Token 만료 시 호출하세요.
                        - 성공 시 새로운 Access Token과 Refresh Token이 쿠키로 설정됩니다.
                        - Refresh Token도 만료된 경우 재로그인이 필요합니다.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = """
                                        AUTH_004: 리프레시 토큰 만료 (재로그인 필요)
                                        AUTH_005: 토큰이 존재하지 않음 (쿠키 없음)
                                        """)
        })
        @PostMapping("/reissue")
        public ResponseEntity<ApiResponse<Void>> reissueTokens(
                        HttpServletRequest request,
                        HttpServletResponse response) {

                authService.reissueTokens(request, response);

                return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공"));
        }

        /**
         * 로그아웃
         */
        @Operation(summary = "로그아웃", description = """
                        현재 로그인된 사용자를 로그아웃합니다.

                        - Redis에 저장된 Refresh Token이 삭제됩니다.
                        - 브라우저의 Access Token, Refresh Token 쿠키가 삭제됩니다.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
        })
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        HttpServletRequest request,
                        HttpServletResponse response) {

                authService.logout(request, response);

                return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
        }
}
