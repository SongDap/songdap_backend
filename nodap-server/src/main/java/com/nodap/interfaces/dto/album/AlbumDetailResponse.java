package com.nodap.interfaces.dto.album;

import com.nodap.domain.album.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumDetailResponse {
    private String uuid;
    private String title;
    private String description;
    private Boolean isPublic;
    private Integer musicCount;
    private Integer musicCountLimit;
}
