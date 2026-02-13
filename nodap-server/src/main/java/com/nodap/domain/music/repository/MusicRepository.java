package com.nodap.domain.music.repository;

import com.nodap.domain.album.entity.Album;
import com.nodap.domain.music.entity.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 수록곡 Repository
 */
@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    /**
     * UUID로 수록곡 조회
     */
    Optional<Music> findByUuid(String uuid);

    /**
     * UUID로 수록곡 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT m FROM Music m WHERE m.uuid = :uuid AND m.deletedAt IS NULL")
    Optional<Music> findByUuidAndNotDeleted(@Param("uuid") String uuid);

    /**
     * 앨범 ID로 수록곡 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT m FROM Music m WHERE m.album.id = :albumId AND m.deletedAt IS NULL")
    Page<Music> findByAlbumIdAndNotDeleted(@Param("albumId") Long albumId, Pageable pageable);

    /**
     * 앨범의 수록곡 개수 조회
     */
    @Query("SELECT COUNT(m) FROM Music m WHERE m.album.id = :albumId AND m.deletedAt IS NULL")
    long countByAlbumIdAndNotDeleted(@Param("albumId") Long albumId);

    /**
     * 노래 삭제 가능 여부 조회
     */
    @Query("SELECT COUNT(m) > 0 FROM Music m JOIN m.album a " +
            "WHERE m.uuid = :musicUuid AND a.deletedAt IS NULL AND a.user.id = :userId") // 추후에 노래 등록자도 가능하게 하고 싶으면 or m.writer.id = :userId 추가 가능
    boolean canDeleteMusic(@Param("musicUuid") String musicUuid, @Param("userId") Long userId);

    /**
     * uuid로 노래 삭제
     */
    void deleteByUuid(String uuid);
}



