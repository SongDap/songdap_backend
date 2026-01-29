package com.nodap.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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
        String accessKey = s3Properties.accessKey();
        String secretKey = s3Properties.secretKey();
        String bucket = s3Properties.bucket();

        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException(
                "S3 버킷 이름이 설정되지 않았습니다. " +
                "환경 변수 AWS_S3_BUCKET 또는 application-local.yml의 aws.s3.bucket을 확인하세요."
            );
        }

        var builder = S3Client.builder()
                .region(Region.of(s3Properties.region()));

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            // Use static credentials from properties/env
            if (secretKey.trim().length() != secretKey.length()) {
                log.warn("[S3 Config] ⚠️ Secret Key에 앞뒤 공백이 있습니다! trim() 처리합니다.");
                secretKey = secretKey.trim();
            }
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey.trim(), secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
            log.info("[S3 Config] Using static AWS credentials. Access Key: {} (len={})",
                    accessKey.substring(0, Math.min(8, accessKey.length())) + "***", accessKey.length());
        } else {
            // Fallback to DefaultCredentialsProvider (e.g., EC2 Instance Profile)
            builder.credentialsProvider(DefaultCredentialsProvider.create());
            log.info("[S3 Config] Using DefaultCredentialsProvider (instance role or environment credentials)");
        }

        log.info("[S3 Config] Bucket: {}", bucket);

        return builder.build();
    }
}
