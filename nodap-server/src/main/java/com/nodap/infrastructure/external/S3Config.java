package com.nodap.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 설정 클래스
 */
@Slf4j
@Configuration
public class S3Config {

    private final S3Properties s3Properties;

    public S3Config(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }

    /**
     * S3 클라이언트 빈 생성
     */
    @Bean
    public S3Client s3Client() {
        // 자격 증명 검증 (이중 체크)
        String accessKey = s3Properties.accessKey();
        String secretKey = s3Properties.secretKey();
        String bucket = s3Properties.bucket();
        
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalStateException(
                "AWS Access Key가 설정되지 않았습니다. " +
                "환경 변수 AWS_ACCESS_KEY 또는 application-local.yml의 aws.s3.access-key를 확인하세요."
            );
        }
        
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                "AWS Secret Key가 설정되지 않았습니다. " +
                "환경 변수 AWS_SECRET_KEY 또는 application-local.yml의 aws.s3.secret-key를 확인하세요."
            );
        }
        
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException(
                "S3 버킷 이름이 설정되지 않았습니다. " +
                "환경 변수 AWS_S3_BUCKET 또는 application-local.yml의 aws.s3.bucket을 확인하세요."
            );
        }
        
        // 디버깅: 로드된 자격 증명 정보 확인 (민감 정보는 마스킹)
        log.info("[S3 Config] Access Key: {} (길이: {})", 
            accessKey.substring(0, Math.min(8, accessKey.length())) + "***", 
            accessKey.length());
        log.info("[S3 Config] Secret Key 길이: {}", secretKey.length());
        log.info("[S3 Config] Bucket: {}", bucket);
        log.info("[S3 Config] Region: {}", s3Properties.region());
        
        // Secret Key에 공백이나 줄바꿈이 있는지 확인
        if (secretKey.trim().length() != secretKey.length()) {
            log.warn("[S3 Config] ⚠️ Secret Key에 앞뒤 공백이 있습니다! trim() 처리합니다.");
            secretKey = secretKey.trim();
        }
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey.trim(), secretKey);

        return S3Client.builder()
                .region(Region.of(s3Properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
