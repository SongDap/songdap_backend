package com.nodap.interfaces.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicDetailResponse {
    private MusicInfo musics;
    private Flag flag;

    @Getter
    @AllArgsConstructor
    public static class Flag{
        private boolean isOwner;
        private boolean canDelete;
    }
}
