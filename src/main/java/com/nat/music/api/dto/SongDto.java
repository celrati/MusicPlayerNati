package com.nat.music.api.dto;


import com.nat.music.api.domain.enums.Genre;

public record SongDto(
        Long id,
        String title,
        String artist,
        Genre genre,
        int duration,
        String filePath
) {}