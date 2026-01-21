package com.nodap.application.music;

public interface MusicVideoSearchPort {

    /**
     * 아티스트와 제목을 기반으로
     * 공식 MV URL을 반환한다.
     *
     * @param artist 가수명
     * @param title  노래 제목
     * @return 영상 URL
     */
    String search(String artist, String title);
}
