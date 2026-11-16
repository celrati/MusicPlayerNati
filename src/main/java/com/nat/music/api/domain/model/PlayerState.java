package com.nat.music.api.domain.model;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.PlayerStatus;

public record PlayerState(
        Song currentSong,
        PlayerStatus status
) {}
