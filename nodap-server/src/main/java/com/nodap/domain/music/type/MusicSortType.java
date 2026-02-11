package com.nodap.domain.music.type;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public enum MusicSortType {
        LATEST,   // 최신순
        OLDEST,   // 오래된 순
        TITLE,   // 제목순
        ARTIST; // 가수순

        /**
         * 정렬 기준을 Spring Data Sort로 변환
         */
        public Sort toSort() {
            return switch (this) {
                case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
                case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt");
                case TITLE  -> Sort.by(Sort.Direction.ASC, "title");
                case ARTIST -> Sort.by(Sort.Direction.ASC, "artist");
            };
        }

        /**
         * Pageable 생성 (page, size + sort)
         */
        public Pageable toPageable(int page, int size) {
            return PageRequest.of(page, size, this.toSort());
        }
}


