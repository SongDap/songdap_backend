package com.nodap.infrastructure.external.youtube;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * YouTube API 설정 프로퍼티
 * 생성자 바인딩 사용
 */

@ConfigurationProperties(prefix = "youtube")
public record YoutubeProperties(
        String apiKey
) {
}
