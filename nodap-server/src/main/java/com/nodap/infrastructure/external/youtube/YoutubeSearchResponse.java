package com.nodap.infrastructure.external.youtube;

import lombok.Getter;

import java.util.List;

@Getter
public class YoutubeSearchResponse {

    private List<Item> items;

    @Getter
    public static class Item {
        private Id id;
    }

    @Getter
    public static class Id {
        private String videoId;
    }
}
