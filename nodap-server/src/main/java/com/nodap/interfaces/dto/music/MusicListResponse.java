package com.nodap.interfaces.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class MusicListResponse {

    private Flag flag;
    private Page<MusicInfo> items;

    @Getter
    @AllArgsConstructor
    public static class Flag{
        private boolean isOwner;
        private boolean canDelete;
        private boolean canAdd;
    }
}

