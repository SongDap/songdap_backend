package com.nodap.domain.album.repository;

import com.nodap.domain.album.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 앨범 Repository
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    /**
     * UUID로 앨범 조회
     */
    Optional<Album> findByUuid(String uuid);

    /**
     * UUID로 앨범 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT a FROM Album a WHERE a.uuid = :uuid AND a.deletedAt IS NULL")
    Optional<Album> findByUuidAndNotDeleted(@Param("uuid") String uuid);

    /**
     * 사용자 ID로 앨범 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT a FROM Album a WHERE a.user.id = :userId AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<Album> findByUserIdAndNotDeleted(@Param("userId") Long userId);

    /**
     * 사용자 ID로 앨범 목록 페이징 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT a FROM Album a WHERE a.user.id = :userId AND a.deletedAt IS NULL")
    Page<Album> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    /**
     * 공개 앨범 목록 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT a FROM Album a WHERE a.isPublic = true AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<Album> findPublicAlbums();

    /**
     * 사용자의 앨범 개수 조회
     */
    @Query("SELECT COUNT(a) FROM Album a WHERE a.user.id = :userId AND a.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    /**
     *  앨범의 소유주인지 조회
     */
    @Query("SELECT COUNT(a) > 0 FROM Album a " +
            "WHERE a.uuid = :albumUuid AND a.user.id = :userId AND a.deletedAt IS NULL")
    boolean isOwner(@Param("albumUuid") String albumUuid, @Param("userId") Long userId);

}



