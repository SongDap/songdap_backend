package com.nodap.interfaces.dto.album;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlbumCreateRequest {

    private String title;
    private String description;
    private Boolean isPublic;
    private Integer musicCountLimit;
    private String color;
}
