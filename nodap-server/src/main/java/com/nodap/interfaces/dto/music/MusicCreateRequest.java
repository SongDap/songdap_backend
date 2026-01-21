package com.nodap.interfaces.dto.music;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MusicCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String artist;

    private String message;
    private String writer;
    private String image;
}
