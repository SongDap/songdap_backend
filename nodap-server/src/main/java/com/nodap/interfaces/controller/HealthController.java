package com.nodap.interfaces.controller;

import com.nodap.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 */
@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 헬스 체크
     */
    @Operation(summary = "헬스 체크", description = """
            서버가 정상적으로 작동하는지 확인합니다.
            
            - 서버 상태(status)와 현재 시간(timestamp)을 반환합니다.
            - 로드밸런서 헬스체크, 모니터링 용도로 사용됩니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "서버 정상 작동",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class))
            )
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success("서버가 정상 작동 중입니다.", data));
    }

    /**
     * 헬스 체크 응답 스키마 (Swagger 문서용)
     */
    @Schema(description = "헬스 체크 응답")
    private record HealthResponse(
            @Schema(description = "서버 상태", example = "UP")
            String status,
            @Schema(description = "현재 시간", example = "2024-01-15T10:30:00")
            String timestamp
    ) {}
}
