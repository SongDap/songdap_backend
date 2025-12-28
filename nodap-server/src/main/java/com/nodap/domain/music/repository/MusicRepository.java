package com.nodap.domain.music.repository;

import com.nodap.domain.music.entity.Music;
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
    @Query("SELECT m FROM Music m WHERE m.album.id = :albumId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<Music> findByAlbumIdAndNotDeleted(@Param("albumId") Long albumId);

    /**
     * 앨범의 수록곡 개수 조회
     */
    @Query("SELECT COUNT(m) FROM Music m WHERE m.album.id = :albumId AND m.deletedAt IS NULL")
    long countByAlbumIdAndNotDeleted(@Param("albumId") Long albumId);
}



