package com.nodap.domain.user.entity;

/**
 * OAuth 제공자 Enum
 */
public enum Provider {
    KAKAO("카카오"),
    GOOGLE("구글");

    private final String description;

    Provider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
