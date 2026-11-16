package com.nat.music.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistResponseDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SongDto> songs
) {}