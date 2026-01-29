package com.nodap.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 설정 프로퍼티
 * 생성자 바인딩 사용 (Setter 금지)
 */
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        /**
         * AWS 리전
         */
        String region,

        /**
         * S3 버킷 이름
         */
        String bucket,

        /**
         * AWS Access Key
         */
        String accessKey,

        /**
         * AWS Secret Key
         */
        String secretKey,

        /**
         * S3 Base URL (CDN 또는 직접 접근 URL)
         */
        String baseUrl
) {
    public S3Properties {
        if (region == null || region.isBlank()) {
            region = "ap-northeast-2";
        }
        
        // 버킷 검증 (버킷 정보는 반드시 필요)
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException(
                "S3 버킷 이름이 설정되지 않았습니다. " +
                "환경 변수 AWS_S3_BUCKET 또는 application-local.yml의 aws.s3.bucket을 설정하세요."
            );
        }
        // Note: accessKey/secretKey may be omitted when using IAM Role (Instance Profile) or
        // DefaultCredentialsProvider. Do not enforce accessKey/secretKey here.
    }
}
