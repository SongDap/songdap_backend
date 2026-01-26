package com.nodap.interfaces.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodap.application.user.UserService;
import com.nodap.global.common.ApiResponse;
import com.nodap.infrastructure.external.S3Folder;
import com.nodap.infrastructure.external.S3Service;
import com.nodap.interfaces.dto.user.CheckNicknameResponse;
import com.nodap.interfaces.dto.user.UpdateUserRequest;
import com.nodap.interfaces.dto.user.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 사용자 API 컨트롤러
 */
@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

    /**
     * 내 정보 조회
     */
    @Operation(summary = "내 정보 조회", description = """
            현재 로그인한 사용자의 정보를 조회합니다.
            
            - 로그인한 사용자만 호출 가능합니다.
            - 사용자 ID, 닉네임, 이메일, 프로필 이미지 URL을 반환합니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "USER_001: 사용자를 찾을 수 없습니다."
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        
        UserInfoResponse userInfo = userService.getMyInfo(userId);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfo));
    }

    /**
     * 내 정보 수정
     */
    @Operation(summary = "내 정보 수정", description = """
            현재 로그인한 사용자의 정보를 수정합니다.
            
            - 로그인한 사용자만 호출 가능합니다.
            - 닉네임은 필수이며, 이메일과 프로필 이미지는 선택입니다.
            - 프로필 이미지는 MultipartFile로 업로드하며, S3에 저장됩니다.
            - 닉네임 중복 시 에러가 발생합니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "USER_001: 사용자를 찾을 수 없습니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "USER_002: 이미 사용 중인 닉네임입니다."
            )
    })
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestPart("request") String requestJson,
            @Parameter(description = "프로필 이미지 파일 (jpg, jpeg, png, gif, webp, 최대 10MB)", 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "file", required = false) @Schema(type = "string", format = "binary") MultipartFile file) throws IOException {
        
        UpdateUserRequest request;
        try {
            request = objectMapper.readValue(requestJson, UpdateUserRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("request는 올바른 JSON 형식이어야 합니다.");
        }
        
        String profileImageUrl = null;
        if (file != null && !file.isEmpty()) {
            profileImageUrl = s3Service.uploadImage(file, S3Folder.USER_PROFILE);
        }
        
        UserInfoResponse userInfo = userService.updateMyInfo(userId, request, profileImageUrl);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 수정 성공", userInfo));
    }

    /**
     * 닉네임 중복 확인
     */
    @Operation(summary = "닉네임 중복 확인", description = """
            입력된 닉네임의 사용 가능 여부를 조회합니다.
            
            - 로그인한 사용자만 호출 가능합니다.
            - 회원가입/수정 시 '저장' 버튼을 누르기 전, 실시간으로 중복 여부를 확인할 수 있습니다.
            - isDuplicated가 true면 이미 사용 중인 닉네임입니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "닉네임 중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = CheckNicknameResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "ERR_001: 파라미터 누락 (nickname 파라미터 누락)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<CheckNicknameResponse>> checkNickname(
            @AuthenticationPrincipal Long userId,
            @RequestParam String nickname) {
        
        CheckNicknameResponse response = userService.checkNickname(nickname);
        String message = response.isDuplicated() 
                ? "이미 사용 중인 닉네임입니다." 
                : "사용 가능한 닉네임입니다.";
        
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 회원 탈퇴
     */
    @Operation(summary = "회원 탈퇴", description = """
            회원의 모든 정보를 삭제하고 카카오 연동을 해제합니다.
            
            - 로그인한 사용자만 호출 가능합니다.
            - 사용자 정보는 Soft Delete 처리됩니다.
            - OAuth 계정 정보도 삭제됩니다.
            - Redis에 저장된 Refresh Token이 삭제됩니다.
            - 브라우저의 Access Token, Refresh Token 쿠키가 삭제됩니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "USER_001: 사용자를 찾을 수 없습니다."
            )
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {
        
        userService.withdraw(userId, response);
        
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 성공"));
    }
}
