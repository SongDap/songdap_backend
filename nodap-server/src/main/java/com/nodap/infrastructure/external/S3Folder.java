package com.nodap.infrastructure.external;

/**
 * S3 폴더 경로 상수
 * 이미지 타입별로 폴더를 구분하여 관리
 */
public enum S3Folder {
    /**
     * 노래 이미지
     * 예: songs/uuid.jpg
     */
    SONGS("songs"),

    /**
     * 앨범 커버 이미지
     * 예: albums/cover/uuid.jpg
     */
    ALBUM_COVER("albums/cover"),

    /**
     * LP 원형 이미지
     * 예: albums/lp/uuid.jpg
     */
    ALBUM_LP("albums/lp"),

    /**
     * 사용자 프로필 이미지
     * 예: users/profile/uuid.jpg
     */
    USER_PROFILE("users/profile");

    private final String path;

    S3Folder(String path) {
        this.path = path;
    }

    /**
     * 폴더 경로 반환
     */
    public String getPath() {
        return path;
    }
}
