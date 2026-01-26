package com.nodap.interfaces.controller;

import com.nodap.application.album.AlbumService;
import com.nodap.domain.album.type.AlbumSortType;
import com.nodap.global.common.ApiResponse;
import com.nodap.interfaces.dto.album.AlbumCreateRequest;
import com.nodap.interfaces.dto.album.AlbumCreateResponse;
import com.nodap.interfaces.dto.album.AlbumDetailResponse;
import com.nodap.interfaces.dto.album.AlbumListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Album", description = "앨범 관련 API")
@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    /**
     * 앨범 생성
     */
    @Operation(
            summary = "앨범 생성",
            description = """
                    새로운 앨범을 생성합니다.

                    - 로그인한 사용자만 호출 가능합니다.
                    - 앨범 제목은 필수 입력값입니다.
                    - 성공 시 앨범이 생성됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앨범 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @PostMapping()
    public ResponseEntity<ApiResponse<AlbumCreateResponse>> createAlbum(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AlbumCreateRequest request){
        AlbumCreateResponse response = albumService.createAlbum(userId, request);
        return ResponseEntity.ok(ApiResponse.success("앨범 생성 성공", response));
    }

    /**
     * 앨범 목록 조회
     */
    @Operation(
            summary = "앨범 목록 조회",
            description = """
                    사용자의 앨범 목록을 조회합니다.

                    - 정렬 기준과 페이지 정보를 전달할 수 있습니다.
                    - 기본 정렬은 최신순(LATEST)입니다.
                    - 페이지 번호는 0부터 시작합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앨범 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<AlbumListResponse>>> getAlbumList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "LATEST") AlbumSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AlbumListResponse> response = albumService.getAlbumList(userId, sort, page, size);

        return ResponseEntity.ok(ApiResponse.success("앨범 목록 조회 성공", response));
    }

    /**
     * 앨범 상세 조회
     */
    @Operation(
            summary = "앨범 상세 조회",
            description = """
                    앨범 UUID를 이용해 특정 앨범의 상세 정보를 조회합니다.

                    - 본인이 생성한 앨범만 조회할 수 있습니다.
                    - 존재하지 않는 앨범일 경우 에러가 발생합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앨범 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "앨범 접근 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "앨범을 찾을 수 없음"
            )
    })
    @GetMapping("/{albumUuid}")
    public ResponseEntity<ApiResponse<AlbumDetailResponse>> getAlbum(
            @AuthenticationPrincipal Long userId,
            @PathVariable String albumUuid
    ) {
        AlbumDetailResponse response = albumService.getAlbumByUuid(userId, albumUuid);

        return ResponseEntity.ok(ApiResponse.success("앨범 조회 성공", response));
    }

    /**
     * 앨범 삭제
     */
    @Operation(
            summary = "앨범 삭제",
            description = """
                    앨범 UUID를 이용해 앨범을 삭제합니다.

                    - 본인이 생성한 앨범만 삭제할 수 있습니다.
                    - 실제 데이터는 소프트 삭제 처리됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "앨범 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "앨범 삭제 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "앨범을 찾을 수 없음"
            )
    })
    @DeleteMapping("/{albumUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(
            @AuthenticationPrincipal Long userId,
            @PathVariable String albumUuid
    ) {
        albumService.deleteAlbum(userId, albumUuid);

        return ResponseEntity.ok(ApiResponse.success("앨범 삭제 성공"));
    }
}