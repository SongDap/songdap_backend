package com.nodap.infrastructure.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 파일 업로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * 이미지 파일을 S3에 업로드하고 URL을 반환
     *
     * @param file 업로드할 파일
     * @param folder S3 폴더 타입
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 읽기 실패 시
     */
    public String uploadImage(MultipartFile file, S3Folder folder) throws IOException {
        return uploadImage(file, folder.getPath());
    }

    /**
     * 이미지 파일을 S3에 업로드하고 URL을 반환
     *
     * @param file 업로드할 파일
     * @param folderPath S3 내 폴더 경로 (예: "songs", "albums/cover")
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 읽기 실패 시
     */
    public String uploadImage(MultipartFile file, String folderPath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }

        // 파일 크기 검증 (최대 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 고유한 파일명 생성
        String fileName = generateFileName(folderPath, extension);

        try {
            // ACL 설정 제거: 최신 AWS S3 버킷은 기본적으로 ACL을 비활성화합니다.
            // 대신 버킷 정책을 사용하여 퍼블릭 읽기 권한을 부여해야 합니다.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            // 업로드된 파일의 URL 생성
            String fileUrl = s3Properties.baseUrl() + "/" + fileName;
            log.info("[S3] 이미지 업로드 성공: fileName={}, url={}", fileName, fileUrl);

            return fileUrl;

        } catch (S3Exception e) {
            log.error("[S3] 이미지 업로드 실패: fileName={}, error={}", fileName, e.getMessage(), e);
            
            // AWS 자격 증명 관련 에러인 경우 더 명확한 메시지 제공
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("Access Key Id") || errorMessage.contains("does not exist")) {
                    throw new RuntimeException(
                        "AWS 자격 증명이 올바르지 않습니다. " +
                        "application-local.yml의 aws.s3.access-key와 aws.s3.secret-key를 확인하거나 " +
                        "환경 변수 AWS_ACCESS_KEY와 AWS_SECRET_KEY를 확인하세요. " +
                        "원본 에러: " + errorMessage, e
                    );
                } else if (errorMessage.contains("Access Denied") || errorMessage.contains("403")) {
                    throw new RuntimeException(
                        "AWS 자격 증명에 S3 버킷 접근 권한이 없습니다. " +
                        "IAM 사용자의 권한을 확인하세요. 원본 에러: " + errorMessage, e
                    );
                }
            }
            
            throw new RuntimeException("S3 업로드 실패: " + errorMessage, e);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 이미지 확장자 검증
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") ||
               extension.equals("jpeg") ||
               extension.equals("png") ||
               extension.equals("gif") ||
               extension.equals("webp");
    }

    /**
     * 고유한 파일명 생성
     * 형식: {folderPath}/{uuid}.{extension}
     * 
     * @param folderPath 폴더 경로 (예: "songs", "albums/cover")
     * @param extension 파일 확장자 (예: "jpg", "png")
     * @return S3 키 (예: "songs/uuid.jpg")
     */
    private String generateFileName(String folderPath, String extension) {
        String uuid = UUID.randomUUID().toString();
        return folderPath + "/" + uuid + "." + extension;
    }

    /**
     * S3에서 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // URL에서 파일 키 추출
            String fileKey = extractFileKeyFromUrl(fileUrl);
            if (fileKey == null) {
                log.warn("[S3] 파일 키 추출 실패: url={}", fileUrl);
                return;
            }

            s3Client.deleteObject(builder -> builder
                    .bucket(s3Properties.bucket())
                    .key(fileKey)
                    .build());

            log.info("[S3] 파일 삭제 성공: fileKey={}", fileKey);

        } catch (S3Exception e) {
            log.error("[S3] 파일 삭제 실패: url={}, error={}", fileUrl, e.getMessage(), e);
        }
    }

    /**
     * URL에서 파일 키 추출
     * 예: https://bucket.s3.region.amazonaws.com/songs/uuid.jpg -> songs/uuid.jpg
     */
    private String extractFileKeyFromUrl(String fileUrl) {
        try {
            // baseUrl이 포함되어 있는지 확인
            String baseUrl = s3Properties.baseUrl();
            if (fileUrl.startsWith(baseUrl)) {
                return fileUrl.substring(baseUrl.length() + 1); // +1은 '/' 제거
            }

            // 직접 버킷 URL 형식인 경우
            int bucketIndex = fileUrl.indexOf(s3Properties.bucket());
            if (bucketIndex != -1) {
                int keyStartIndex = fileUrl.indexOf('/', bucketIndex + s3Properties.bucket().length()) + 1;
                if (keyStartIndex > 0 && keyStartIndex < fileUrl.length()) {
                    return fileUrl.substring(keyStartIndex);
                }
            }

            return null;
        } catch (Exception e) {
            log.error("[S3] URL 파싱 실패: url={}", fileUrl, e);
            return null;
        }
    }
}
