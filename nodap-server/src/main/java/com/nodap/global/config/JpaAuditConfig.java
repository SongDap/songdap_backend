package com.nodap.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정
 * BaseTimeEntity의 @CreatedDate, @LastModifiedDate 자동 적용을 위해 필요
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
