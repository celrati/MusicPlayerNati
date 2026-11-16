package com.nat.music.api.service.playerservice.impl;

import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistSongManagerServiceImpl#addSongToPlaylist}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations.
 */
@ExtendWith(MockitoExtension.class)
class AddSongToPlaylistTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;

    private PlaylistSongManagerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = newSongManagerService(playlistRepository, songRepository);
    }

    /**
     * The song is appended after the existing songs and {@code updatedAt} is refreshed.
     */
    @Test
    void appendsSongAtTheEndAndRefreshesUpdatedAt() {
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(playlist(3L, bohemian())));
        when(songRepository.findById(2L)).thenReturn(Optional.of(stairway()));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponseDto result = service.addSongToPlaylist(3L, 2L);

        assertThat(titles(result)).containsExactly("Bohemian Rhapsody", "Stairway to Heaven");
        assertThat(result.updatedAt()).isAfter(OLD_TIMESTAMP);
    }

    /**
     * A missing playlist fails fast: the song repository is not queried and nothing is saved.
     */
    @Test
    void unknownPlaylist_throwsAndDoesNotLookUpSong() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addSongToPlaylist(9999L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
        verifyNoInteractions(songRepository);
        verify(playlistRepository, never()).save(any());
    }

    /**
     * A missing song raises {@code EntityNotFoundException}; the playlist keeps its songs and
     * its {@code updatedAt}, and nothing is saved.
     */
    @Test
    void unknownSong_throwsAndLeavesPlaylistUntouched() {
        Song bohemian = bohemian();
        Playlist existing = playlist(3L, bohemian);
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(songRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addSongToPlaylist(3L, 9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Song not found with id: 9999");
        assertThat(existing.getSongs()).containsExactly(bohemian);
        assertThat(existing.getUpdatedAt()).isEqualTo(OLD_TIMESTAMP);
        verify(playlistRepository, never()).save(any());
    }
}
