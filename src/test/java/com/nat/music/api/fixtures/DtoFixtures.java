package com.nat.music.api.fixtures;


import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.dto.SongDto;

import java.time.LocalDateTime;
import java.util.List;

/** Ready-made DTOs for the controller tests. */
public final class DtoFixtures {

    public static final String BASE_URL = "/api/v1/playlists";
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    public static final SongDto BOHEMIAN =
            new SongDto(1L, "Bohemian Rhapsody", "Queen", Genre.ROCK, 354, "/music/queen-bohemian.mp3");
    public static final SongDto STAIRWAY =
            new SongDto(2L, "Stairway to Heaven", "Led Zeppelin", Genre.ROCK, 482, "/music/led-zeppelin-stairway.mp3");

    private DtoFixtures() {
    }

    public static PlaylistResponseDto playlistDto(Long id, String name, String description, SongDto... songs) {
        return new PlaylistResponseDto(id, name, description, NOW, NOW, List.of(songs));
    }
}
