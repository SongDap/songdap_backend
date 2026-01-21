package com.nodap.infrastructure.external.youtube;

import com.nodap.application.music.MusicVideoSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class YoutubeSearchClient implements MusicVideoSearchPort {

    private final YoutubeProperties youtubeProperties;
    private final WebClient webClient;

    @Override
    public String search(String artist, String title) {

        String query = artist + " " + title + " official mv";

        YoutubeSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "video")
                        .queryParam("videoCategoryId", "10")
                        .queryParam("maxResults", 1)
                        .queryParam("key", youtubeProperties.apiKey()) // ⭐ 수정
                        .build())
                .retrieve()
                .bodyToMono(YoutubeSearchResponse.class)
                .block();

        if (response == null || response.getItems().isEmpty()) {
            return null;
        }

        String videoId = response.getItems().get(0).getId().getVideoId();
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
