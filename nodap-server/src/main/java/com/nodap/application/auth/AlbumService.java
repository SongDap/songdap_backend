package com.nodap.application.auth;

import com.nodap.domain.album.entity.Album;
import com.nodap.domain.album.entity.Category;
import com.nodap.domain.album.repository.AlbumRepository;
import com.nodap.domain.album.type.AlbumSortType;
import com.nodap.domain.user.entity.User;
import com.nodap.domain.user.repository.UserRepository;
import com.nodap.interfaces.dto.album.AlbumCreateRequest;
import com.nodap.interfaces.dto.album.AlbumDetailResponse;
import com.nodap.interfaces.dto.album.AlbumListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 앨범의 CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    /**
     * 앨범 생성
     */
    @Transactional
    public void createAlbum(Long userId, AlbumCreateRequest request){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        Album album = Album.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .color(request.getColor())
                .isPublic(request.getIsPublic())
                .musicCountLimit(request.getMusicCountLimit())
                .build();

        albumRepository.save(album);
    }

    /**
     * 내 앨범 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<AlbumListResponse> getAlbumList(Long userId, AlbumSortType sortType, int page, int size) {
        Pageable pageable = sortType.toPageable(page, size);

        return albumRepository
                .findByUserIdAndNotDeleted(userId, pageable)
                .map(album -> new AlbumListResponse(
                        album.getUuid(),
                        album.getTitle(),
                        album.getColor()
                ));
    }

    /**
     * 앨범 정보 조회
     */
    @Transactional(readOnly = true)
    public AlbumDetailResponse getAlbumByUuid(Long userId, String albumUuid){
        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 앨범"));

        return new AlbumDetailResponse(
                album.getUuid(),
                album.getTitle(),
                album.getDescription(),
                album.getIsPublic(),
                album.getMusicCount(),
                album.getMusicCountLimit()
        );
    }

    /**
     * 앨범 삭제
     */
    @Transactional
    public void deleteAlbum(Long userId, String albumUuid) {

        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 앨범"));

        if (!album.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("앨범 삭제 권한이 없습니다.");
        }

        album.delete();
    }
}
