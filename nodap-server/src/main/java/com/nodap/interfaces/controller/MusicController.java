package com.nodap.interfaces.controller;


import com.nodap.application.music.MusicService;
import com.nodap.domain.music.type.MusicSortType;
import com.nodap.global.common.ApiResponse;
import com.nodap.interfaces.dto.music.MusicCreateRequest;
import com.nodap.interfaces.dto.music.MusicListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Music", description = "노래(수록곡) 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MusicController {
    private final MusicService musicService;

    /**
     * 노래 등록 (로그인 불필요)
     */
    @Operation(
            summary = "노래 등록",
            description = """
                앨범에 새로운 노래(메시지 카드)를 등록합니다.

                - 로그인 없이 호출 가능합니다.
                - 앨범 UUID는 필수 입력값입니다.
                - 노래 제목, 아티스트는 필수 입력값입니다.
                - 카드 위치 정보(posX, posY, cardLength)는 필수입니다.
                - 성공 시 노래가 앨범에 추가됩니다.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "노래 등록 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "앨범을 찾을 수 없음"
            )
    })
    @PostMapping("albums/{albumUuid}/musics")
    public ResponseEntity<ApiResponse<Void>> createMusic(
            @PathVariable String albumUuid,
            @Valid @RequestBody MusicCreateRequest request) {
        musicService.createMusic(albumUuid, request);
        return ResponseEntity.ok(ApiResponse.success("노래 등록 성공"));
    }

    /**
     * 노래 목록 조회
     */
    @Operation(
            summary = "노래 목록 조회",
            description = """
                    특정 앨범에 포함된 노래 목록을 조회합니다.

                    - 로그인 없이 호출 가능합니다.
                    - 앨범 UUID 기준으로 노래 목록을 반환합니다.
                    - 응답에는 노래 리스트와 마지막 페이지 번호(lastPageNo)가 포함됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노래 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "앨범 UUID가 유효하지 않음"
            )
    })
    @GetMapping("/{albumUuid}/musics")
    public ResponseEntity<ApiResponse<Page<MusicListResponse>>> getMusicList(
            @AuthenticationPrincipal Long userId,
            @PathVariable String albumUuid,
            @RequestParam(defaultValue = "LATEST") MusicSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<MusicListResponse> response = musicService.getMusicList(userId, albumUuid, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success("앨범 목록 조회 성공", response));
    }
}