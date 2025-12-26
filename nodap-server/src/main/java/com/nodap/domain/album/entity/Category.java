package com.nodap.domain.album.entity;

/**
 * 앨범 카테고리 Enum
 * 상황 또는 기분 기반으로 앨범 분류
 */
public enum Category {
    SITUATION("상황"),
    MOOD("기분");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
