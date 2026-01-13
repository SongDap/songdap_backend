package com.nodap.interfaces.dto.album;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlbumListResponse {
    private String uuid;
    private String title;
    private String color;
}
