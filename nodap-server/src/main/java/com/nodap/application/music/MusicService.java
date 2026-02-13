package com.nodap.application.music;

import com.nodap.domain.album.entity.Album;
import com.nodap.domain.album.repository.AlbumRepository;
import com.nodap.domain.music.entity.Music;
import com.nodap.domain.music.repository.MusicRepository;
import com.nodap.domain.music.type.MusicSortType;
import com.nodap.global.error.BusinessException;
import com.nodap.global.error.ErrorCode;
import com.nodap.infrastructure.external.S3Service;
import com.nodap.interfaces.dto.music.MusicCreateRequest;
import com.nodap.interfaces.dto.music.MusicDetailResponse;
import com.nodap.interfaces.dto.music.MusicInfo;
import com.nodap.interfaces.dto.music.MusicListResponse;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 수록곡의 CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicVideoSearchPort musicVideoSearchPort;

    private final AlbumRepository albumRepository;
    private final MusicRepository musicRepository;

    private final S3Service s3Service;
    private static final String DEFAULT_IMAGE_URL =
            "https://nodap-images.s3.ap-northeast-2.amazonaws.com/songs/f2480cd9-6dfb-475e-90f4-d31a7ee052d7.png";

    /**
     * 노래 등록
     */
    @Transactional
    public void createMusic(Long userId, String albumUuid, MusicCreateRequest request, String imageUrl){
        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("앨범이 존재하지 않습니다."));

        boolean isOwner = userId==null? false : albumRepository.isOwner(albumUuid, userId);

        if(!album.getIsPublic() || isOwner){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if(album.getMusicCountLimit()<=album.getMusicCount()){
            throw new BusinessException(ErrorCode.MUSIC_LIMIT_EXCEEDED);
        }

        String videoUrl = musicVideoSearchPort.search(request.getArtist(), request.getTitle());

        if (videoUrl == null || videoUrl.isBlank()) {
            videoUrl = "https://www.youtube.com";
        }

        String writer = request.getWriter();
        if(writer == null || writer.isBlank()){
            writer = "익명";
        }

        Music music = Music.builder()
                .album(album)
                .title(request.getTitle())
                .artist(request.getArtist())
                .message(request.getMessage())
                .url(videoUrl)
                .writer(writer)
                .image(imageUrl)
                .build();

        musicRepository.save(music);
    }

    /**
     * 노래 목록 조회
     */
    @Transactional(readOnly = true)
    public MusicListResponse getMusicList(@Nullable Long userId, String albumUuid, MusicSortType sortType, int page, int size){
        Pageable pageable = sortType.toPageable(page, size);

        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("앨범이 존재하지 않습니다."));

        boolean isOwner = userId==null? false : albumRepository.isOwner(albumUuid, userId);

        if(!album.getIsPublic() && !isOwner){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        boolean canAdd = album.canAddMusic();

        if(isOwner) canAdd = false;

        Page<MusicInfo> items =
                musicRepository.findByAlbumIdAndNotDeleted(album.getId(), pageable)
                        .map(m -> new MusicInfo(
                                m.getUuid(),
                                m.getTitle(),
                                m.getArtist(),
                                m.getMessage(),
                                m.getUrl(),
                                m.getWriter(),
                                m.getImage()
                        ));

        MusicListResponse.Flag flag = new MusicListResponse.Flag(isOwner, isOwner, canAdd);


        return new MusicListResponse(flag, items);
    }

    /**
     * 노래 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public MusicDetailResponse getMusicDetail(Long userId, String musicUuid){
        Music music = musicRepository.findByUuid(musicUuid)
                .orElseThrow(() -> new IllegalArgumentException("노래가 존재하지 않습니다."));

        Album album = music.getAlbum();

        boolean isOwner = userId==null? false : albumRepository.isOwner(album.getUuid(), userId);

        if(!album.getIsPublic() && !isOwner){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        MusicInfo musicInfo = new MusicInfo(
                music.getUuid(),
                music.getTitle(),
                music.getArtist(),
                music.getMessage(),
                music.getUrl(),
                music.getWriter(),
                music.getImage());

        boolean canDelete = userId==null? false : musicRepository.canDeleteMusic(musicUuid, userId);

        return new MusicDetailResponse(musicInfo, new MusicDetailResponse.Flag(isOwner, isOwner));
    }

    /**
     * 노래 삭제 (앨범 주인만 삭제 가능)
     */
    @Transactional
    public void deleteMusic(Long userId, String musicUuid){
        if (userId == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Music music = musicRepository.findByUuid(musicUuid)
                .orElseThrow(() -> new IllegalArgumentException("노래가 존재하지 않습니다."));

        if (!musicRepository.canDeleteMusic(musicUuid, userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Album album = music.getAlbum();

        album.removeMusic(music);

        musicRepository.deleteByUuid(musicUuid);
    }

}
