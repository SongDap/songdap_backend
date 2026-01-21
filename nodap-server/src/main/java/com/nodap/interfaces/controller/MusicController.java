package com.nodap.interfaces.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodap.application.music.MusicService;
import com.nodap.domain.music.type.MusicSortType;
import com.nodap.global.common.ApiResponse;
import com.nodap.infrastructure.external.S3Folder;
import com.nodap.infrastructure.external.S3Service;
import com.nodap.interfaces.dto.music.MusicCreateRequest;
import com.nodap.interfaces.dto.music.MusicDetailResponse;
import com.nodap.interfaces.dto.music.MusicListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Music", description = "노래(수록곡) 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MusicController {
    private final ObjectMapper objectMapper;

    private final MusicService musicService;
    private final S3Service s3Service;

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
    @PostMapping(value = "albums/{albumUuid}/musics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createMusic(
            @AuthenticationPrincipal Long userId,
            @PathVariable String albumUuid,
            @Valid @RequestPart("request") String requestJson,
            @RequestPart(value = "file", required = false) @Schema(type = "string", format = "binary") MultipartFile file) throws IOException {
        MusicCreateRequest request;
        try {
            request = objectMapper.readValue(requestJson, MusicCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("request는 올바른 JSON 형식이어야 합니다.");
        }

        String imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b";

        if(file != null && !file.isEmpty()){
            imageUrl = s3Service.uploadImage(file, S3Folder.SONGS);
        }
        musicService.createMusic(userId, albumUuid, request, imageUrl);
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
    @GetMapping("albums/{albumUuid}/musics")
    public ResponseEntity<ApiResponse<MusicListResponse>> getMusicList(
            @AuthenticationPrincipal Long userId,
            @PathVariable String albumUuid,
            @RequestParam(defaultValue = "LATEST") MusicSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        MusicListResponse response = musicService.getMusicList(userId, albumUuid, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success("노래 목록 조회 성공", response));
    }

    /**
     * 노래 상세 정보 조회
     */
    @Operation(
            summary = "노래 상세 정보 조회",
            description = """
            특정 노래의 상세 정보를 조회합니다.

            - 로그인 없이 호출 가능합니다.
            - 로그인한 경우, 삭제 가능 여부(canDelete)가 함께 반환됩니다.
            - 노래 UUID 기준으로 조회됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노래 상세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "노래가 존재하지 않음"
            )
    })
    @GetMapping("/musics/{musicUuid}")
    public ResponseEntity<ApiResponse<MusicDetailResponse>> getMusicDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable String musicUuid
    ) {
        MusicDetailResponse response =
                musicService.getMusicDetail(userId, musicUuid);

        return ResponseEntity.ok(ApiResponse.success("노래 상세정보 조회 성공", response));
    }


    /**
     * 노래 삭제
     */
    @Operation(
            summary = "노래 삭제",
            description = """
                특정 노래를 삭제합니다.

                - 로그인한 사용자만 호출 가능합니다.
                - 앨범 소유자만 삭제할 수 있습니다.
                - 노래 UUID 기준으로 삭제됩니다.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노래 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "노래 삭제 권한이 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "노래가 존재하지 않음"
            )
    })
    @DeleteMapping("/musics/{musicUuid}")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(
            @AuthenticationPrincipal Long userId,
            @PathVariable String musicUuid
    ) {
        musicService.deleteMusic(userId, musicUuid);
        return ResponseEntity.ok(ApiResponse.success("노래 삭제 성공"));
    }

}