package com.nodap.interfaces.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicListResponse {
    private String uuid;
    private String title;
    private String artist;
    private String url;
    private String image;
}
