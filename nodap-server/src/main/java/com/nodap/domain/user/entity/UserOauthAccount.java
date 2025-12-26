package com.nodap.domain.user.entity;

import com.nodap.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 사용자 OAuth 계정 엔티티
 * 카카오, 구글 등 소셜 로그인 정보를 저장
 */
@Entity
@Table(name = "user_oauth_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "provider", "provider_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauthAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 64)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 256)
    private String providerId;

    @Builder
    public UserOauthAccount(User user, Provider provider, String providerId) {
        this.uuid = UUID.randomUUID().toString();
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;

        // 양방향 연관관계 설정
        if (user != null) {
            user.linkOauthAccount(this);
        }
    }

    /**
     * Provider ID 업데이트 (필요시)
     */
    public void updateProviderId(String providerId) {
        this.providerId = providerId;
    }
}
