package com.nodap.interfaces.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicInfo {
    private String uuid;
    private String title;
    private String artist;
    private String message;
    private String url;
    private String writer;
    private String image;
}
