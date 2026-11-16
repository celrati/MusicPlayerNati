package com.nat.music.api.fixtures;



import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.dto.SongDto;
import com.nat.music.api.mapper.PlaylistMapper;
import com.nat.music.api.mapper.SongMapper;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import com.nat.music.api.service.playerservice.impl.PlaylistServiceImpl;
import com.nat.music.api.service.playerservice.impl.PlaylistSongManagerServiceImpl;
import com.nat.music.api.service.playerservice.impl.PlaylistSongPremiumFeatureServiceImpl;
import com.nat.music.api.strategy.ShuffleStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Entities and a service factory for the service unit tests. */
public final class EntityFixtures {

    public static final LocalDateTime OLD_TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

    private EntityFixtures() {
    }

    public static Song bohemian() {
        return song(1L, "Bohemian Rhapsody", "Queen");
    }

    public static Song stairway() {
        return song(2L, "Stairway to Heaven", "Led Zeppelin");
    }

    public static Song backInBlack() {
        return song(3L, "Back in Black", "AC/DC");
    }

    public static Song song(Long id, String title, String artist) {
        return song(id, title, artist, Genre.ROCK);
    }

    public static Song song(Long id, String title, String artist, Genre genre) {
        return Song.builder().id(id).title(title).artist(artist).genre(genre).duration(300).build();
    }

    /** A playlist whose timestamps are in the past, so a refreshed updatedAt is easy to detect. */
    public static Playlist playlist(Long id, Song... songs) {
        return Playlist.builder()
                .id(id)
                .name("Road Trip")
                .description("Songs for the car")
                .createdAt(OLD_TIMESTAMP)
                .updatedAt(OLD_TIMESTAMP)
                .songs(new ArrayList<>(List.of(songs)))
                .build();
    }

    public static List<String> titles(PlaylistResponseDto dto) {
        return dto.songs().stream().map(SongDto::title).toList();
    }

    /** Builds the service with mocked repositories and the real mappers. */
    public static PlaylistServiceImpl newService(PlaylistRepository playlistRepository,
                                                 SongRepository songRepository,
                                                 Map<String, ShuffleStrategy> strategies) {
        SongMapper songMapper = new SongMapper();
        return new PlaylistServiceImpl(playlistRepository, songRepository,
                new PlaylistMapper(songMapper), songMapper, strategies);
    }

    /** Builds the song manager service with mocked repositories and the real mapper. */
    public static PlaylistSongManagerServiceImpl newSongManagerService(PlaylistRepository playlistRepository,
                                                                       SongRepository songRepository) {
        return new PlaylistSongManagerServiceImpl(playlistRepository, songRepository,
                new PlaylistMapper(new SongMapper()));
    }

    /** Builds the premium feature service (shuffle, recommendations) with mocked repositories and the real mapper. */
    public static PlaylistSongPremiumFeatureServiceImpl newPremiumFeatureService(PlaylistRepository playlistRepository,
                                                                                 SongRepository songRepository,
                                                                                 Map<String, ShuffleStrategy> strategies) {
        return new PlaylistSongPremiumFeatureServiceImpl(playlistRepository, songRepository,
                new SongMapper(), strategies);
    }
}
