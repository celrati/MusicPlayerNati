package com.nat.music.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PlaylistRequestDto(
        @NotBlank(message = "Playlist name cannot be blank")
        String name,
        String description
) {}