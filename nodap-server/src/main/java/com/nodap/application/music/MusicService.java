package com.nodap.application.music;

import com.nodap.domain.album.entity.Album;
import com.nodap.domain.album.repository.AlbumRepository;
import com.nodap.domain.music.entity.Music;
import com.nodap.domain.music.repository.MusicRepository;
import com.nodap.domain.music.type.MusicSortType;
import com.nodap.interfaces.dto.music.MusicCreateRequest;
import com.nodap.interfaces.dto.music.MusicListResponse;
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

    private static final String DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1548199973-03cce0bbc87b";

    /**
     * 노래 등록
     */
    @Transactional
    public void createMusic(String albumUuid, MusicCreateRequest request){
        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("앨범이 존재하지 않습니다."));

        String videoUrl = musicVideoSearchPort.search(request.getArtist(), request.getTitle());

        if (videoUrl == null || videoUrl.isBlank()) {
            videoUrl = "https://www.youtube.com";
        }
        log.info("videoUrl = [{}]", videoUrl);


        String imageUrl = request.getImage();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = DEFAULT_IMAGE_URL;
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
    public Page<MusicListResponse> getMusicList(Long userId, String albumUuid, MusicSortType sortType, int page, int size){
        Pageable pageable = sortType.toPageable(page, size);
        Album album = albumRepository.findByUuidAndNotDeleted(albumUuid)
                .orElseThrow(() -> new IllegalArgumentException("앨범이 존재하지 않습니다."));

        return musicRepository.findByAlbumIdAndNotDeleted(album.getId(), pageable)
                        .map(m -> new MusicListResponse(
                                m.getUuid(),
                                m.getTitle(),
                                m.getArtist(),
                                m.getUrl(),
                                m.getImage()
                        ));

    }
}
