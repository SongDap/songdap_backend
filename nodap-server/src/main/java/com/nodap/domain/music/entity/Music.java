package com.nodap.domain.music.entity;

import com.nodap.domain.album.entity.Album;
import com.nodap.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 수록곡 엔티티
 * 앨범에 포함된 개별 음악 정보를 관리
 */
@Entity
@Table(name = "musics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Music extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "artist", length = 64)
    private String artist;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @Column(name = "writer", nullable = false, length = 64)
    private String writer;

    @Column(name = "image", nullable = false, length = 512)
    private String image;



    @Builder
    public Music(Album album, String title, String artist, String message, String url, String writer,
                 String image) {
        this.uuid = UUID.randomUUID().toString();
        this.album = album;
        this.title = title;
        this.artist = artist;
        this.message = message;
        this.url = url;
        this.writer = writer;

        // 양방향 연관관계 설정
        if (album != null) {
            album.addMusic(this);
        }
    }

    /**
     * 수록곡 제목 변경
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 아티스트 변경
     */
    public void updateArtist(String artist) {
        this.artist = artist;
    }

    /**
     * 메시지 변경
     */
    public void updateMessage(String message) {
        this.message = message;
    }

    /**
     * URL 변경
     */
    public void updateUrl(String url) {
        this.url = url;
    }

    /**
     * 작성자 변경
     */
    public void updateWriter(String writer) {
        this.writer = writer;
    }

    /**
     * 이미지 변경
     */
    public void updateImage(String image) {
        this.image = image;
    }

}

