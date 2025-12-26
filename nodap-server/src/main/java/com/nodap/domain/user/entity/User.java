package com.nodap.domain.user.entity;

import com.nodap.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "nickname", nullable = false, unique = true, length = 16)
    private String nickname;

    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Column(name = "profile_image", length = 256)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserOauthAccount oauthAccount;

    @Builder
    public User(String nickname, String email, String profileImage, Role role) {
        this.uuid = UUID.randomUUID().toString();
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 프로필 이미지 변경
     */
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * OAuth 계정 연결
     */
    public void linkOauthAccount(UserOauthAccount oauthAccount) {
        this.oauthAccount = oauthAccount;
    }
}
