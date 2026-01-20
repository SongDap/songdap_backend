package com.nodap.interfaces.controller;

import com.nodap.global.common.ApiResponse;
import com.nodap.infrastructure.external.S3Folder;
import com.nodap.infrastructure.external.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 개발용 S3 테스트 API 컨트롤러
 * local 프로필에서만 활성화됨
 */
@Tag(name = "Dev S3", description = "개발용 S3 테스트 API (local 환경 전용)")
@Profile("local")
@RestController
@RequestMapping("/api/v1/dev/s3")
@RequiredArgsConstructor
public class DevS3Controller {

    private final S3Service s3Service;

    /**
     * 노래 이미지 업로드 테스트
     */
    @Operation(
            summary = "노래 이미지 업로드 테스트",
            description = """
                    노래 이미지를 S3에 업로드하여 테스트합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 이미지 파일(jpg, jpeg, png, gif, webp)만 업로드 가능합니다.
                    - 최대 파일 크기: 10MB
                    - 업로드된 이미지의 URL을 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = S3UploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "파일 형식 오류 또는 크기 초과"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "S3 업로드 실패"
            )
    })
    @PostMapping(value = "/upload/songs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<S3UploadResponse>> uploadSongImage(
            @Parameter(description = "업로드할 이미지 파일 (jpg, jpeg, png, gif, webp, 최대 10MB)", 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String imageUrl = s3Service.uploadImage(file, S3Folder.SONGS);
        
        S3UploadResponse response = new S3UploadResponse(
                imageUrl,
                S3Folder.SONGS.getPath(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );
        
        return ResponseEntity.ok(ApiResponse.success("노래 이미지 업로드 성공", response));
    }

    /**
     * 앨범 커버 이미지 업로드 테스트
     */
    @Operation(
            summary = "앨범 커버 이미지 업로드 테스트",
            description = """
                    앨범 커버 이미지를 S3에 업로드하여 테스트합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 이미지 파일(jpg, jpeg, png, gif, webp)만 업로드 가능합니다.
                    - 최대 파일 크기: 10MB
                    """
    )
    @PostMapping(value = "/upload/albums/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<S3UploadResponse>> uploadAlbumCover(
            @Parameter(description = "업로드할 이미지 파일 (jpg, jpeg, png, gif, webp, 최대 10MB)", 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String imageUrl = s3Service.uploadImage(file, S3Folder.ALBUM_COVER);
        
        S3UploadResponse response = new S3UploadResponse(
                imageUrl,
                S3Folder.ALBUM_COVER.getPath(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );
        
        return ResponseEntity.ok(ApiResponse.success("앨범 커버 이미지 업로드 성공", response));
    }

    /**
     * LP 원형 이미지 업로드 테스트
     */
    @Operation(
            summary = "LP 원형 이미지 업로드 테스트",
            description = """
                    LP 원형 이미지를 S3에 업로드하여 테스트합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 이미지 파일(jpg, jpeg, png, gif, webp)만 업로드 가능합니다.
                    - 최대 파일 크기: 10MB
                    """
    )
    @PostMapping(value = "/upload/albums/lp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<S3UploadResponse>> uploadLpImage(
            @Parameter(description = "업로드할 이미지 파일 (jpg, jpeg, png, gif, webp, 최대 10MB)", 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String imageUrl = s3Service.uploadImage(file, S3Folder.ALBUM_LP);
        
        S3UploadResponse response = new S3UploadResponse(
                imageUrl,
                S3Folder.ALBUM_LP.getPath(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );
        
        return ResponseEntity.ok(ApiResponse.success("LP 원형 이미지 업로드 성공", response));
    }

    /**
     * 프로필 이미지 업로드 테스트
     */
    @Operation(
            summary = "프로필 이미지 업로드 테스트",
            description = """
                    사용자 프로필 이미지를 S3에 업로드하여 테스트합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 이미지 파일(jpg, jpeg, png, gif, webp)만 업로드 가능합니다.
                    - 최대 파일 크기: 10MB
                    """
    )
    @PostMapping(value = "/upload/users/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<S3UploadResponse>> uploadProfileImage(
            @Parameter(description = "업로드할 이미지 파일 (jpg, jpeg, png, gif, webp, 최대 10MB)", 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String imageUrl = s3Service.uploadImage(file, S3Folder.USER_PROFILE);
        
        S3UploadResponse response = new S3UploadResponse(
                imageUrl,
                S3Folder.USER_PROFILE.getPath(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );
        
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 업로드 성공", response));
    }

    /**
     * 이미지 삭제 테스트
     */
    @Operation(
            summary = "이미지 삭제 테스트",
            description = """
                    S3에서 이미지를 삭제하여 테스트합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 이미지 URL을 전달하면 S3에서 해당 이미지를 삭제합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 삭제 성공"
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestParam("imageUrl") String imageUrl) {
        
        s3Service.deleteFile(imageUrl);
        
        return ResponseEntity.ok(ApiResponse.success("이미지 삭제 성공"));
    }

    /**
     * S3 설정 정보 조회 (테스트용)
     */
    @Operation(
            summary = "S3 설정 정보 조회",
            description = """
                    현재 설정된 S3 정보를 조회합니다.
                    
                    - local 환경에서만 사용 가능합니다.
                    - 버킷 이름, 리전, base URL 정보를 반환합니다.
                    - Access Key와 Secret Key는 보안상 반환하지 않습니다.
                    """
    )
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, String>>> getS3Config() {
        // S3Properties는 private이므로 간단한 정보만 반환
        Map<String, String> config = Map.of(
                "bucket", "nodap-images",
                "region", "ap-northeast-2",
                "baseUrl", "https://nodap-images.s3.ap-northeast-2.amazonaws.com",
                "note", "Access Key와 Secret Key는 보안상 표시하지 않습니다."
        );
        
        return ResponseEntity.ok(ApiResponse.success("S3 설정 정보 조회 성공", config));
    }

    /**
     * S3 업로드 응답 스키마
     */
    @Schema(description = "S3 이미지 업로드 응답")
    private record S3UploadResponse(
            @Schema(description = "업로드된 이미지 URL", example = "https://nodap-images.s3.ap-northeast-2.amazonaws.com/songs/uuid.jpg")
            String imageUrl,
            
            @Schema(description = "S3 폴더 경로", example = "songs")
            String folderPath,
            
            @Schema(description = "원본 파일명", example = "test-image.jpg")
            String originalFilename,
            
            @Schema(description = "파일 크기 (bytes)", example = "102400")
            Long fileSize,
            
            @Schema(description = "파일 MIME 타입", example = "image/jpeg")
            String contentType
    ) {}
}
