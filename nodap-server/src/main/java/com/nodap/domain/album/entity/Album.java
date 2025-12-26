package com.nodap.domain.album.entity;

import com.nodap.domain.music.entity.Music;
import com.nodap.domain.user.entity.User;
import com.nodap.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 앨범 엔티티
 * 사용자가 생성한 음악 앨범을 관리
 */
@Entity
@Table(name = "albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 128)
    private Category category;

    @Column(name = "category_detail", nullable = false, columnDefinition = "TEXT")
    private String categoryDetail;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "music_count", nullable = false)
    private Integer musicCount;

    @Column(name = "music_count_limit")
    private Integer musicCountLimit;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Music> musics = new ArrayList<>();

    @Builder
    public Album(User user, String title, Category category, String categoryDetail,
            String description, Boolean isPublic, Integer musicCountLimit) {
        this.uuid = UUID.randomUUID().toString();
        this.user = user;
        this.title = title;
        this.category = category;
        this.categoryDetail = categoryDetail;
        this.description = description;
        this.isPublic = isPublic != null ? isPublic : false;
        this.musicCount = 0;
        this.musicCountLimit = musicCountLimit != null ? musicCountLimit : 0; // 0은 무한
    }

    /**
     * 앨범 제목 변경
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 앨범 카테고리 변경
     */
    public void updateCategory(Category category) {
        this.category = category;
    }

    /**
     * 앨범 세부 카테고리 변경
     */
    public void updateCategoryDetail(String categoryDetail) {
        this.categoryDetail = categoryDetail;
    }

    /**
     * 앨범 설명 변경
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 앨범 공개 여부 변경
     */
    public void updateIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * 추천 노래 갯수 제한 변경 (0은 무한)
     */
    public void updateMusicCountLimit(Integer musicCountLimit) {
        this.musicCountLimit = musicCountLimit;
    }

    /**
     * 음악 추가 시 카운트 증가
     */
    public void incrementMusicCount() {
        this.musicCount++;
    }

    /**
     * 음악 삭제 시 카운트 감소
     */
    public void decrementMusicCount() {
        if (this.musicCount > 0) {
            this.musicCount--;
        }
    }

    /**
     * 음악 추가 (연관관계 편의 메서드)
     */
    public void addMusic(Music music) {
        this.musics.add(music);
        incrementMusicCount();
    }

    /**
     * 음악 제거 (연관관계 편의 메서드)
     */
    public void removeMusic(Music music) {
        this.musics.remove(music);
        decrementMusicCount();
    }

    /**
     * 추천 노래 추가 가능 여부 확인
     * 
     * @return true: 추가 가능, false: 제한 초과
     */
    public boolean canAddMusic() {
        // musicCountLimit이 0이면 무한
        if (this.musicCountLimit == null || this.musicCountLimit == 0) {
            return true;
        }
        return this.musicCount < this.musicCountLimit;
    }
}
