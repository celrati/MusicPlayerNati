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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistSongManagerServiceImpl#removeSongFromPlaylist}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations.
 */
@ExtendWith(MockitoExtension.class)
class RemoveSongFromPlaylistTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;

    private PlaylistSongManagerServiceImpl service;

    private final Song bohemian = bohemian();
    private final Song stairway = stairway();
    private final Song backInBlack = backInBlack();

    @BeforeEach
    void setUp() {
        service = newSongManagerService(playlistRepository, songRepository);
    }

    /**
     * Only the song at the given index is removed, the others keep their order, and
     * {@code updatedAt} is refreshed.
     */
    @Test
    void removesSongAtIndexAndRefreshesUpdatedAt() {
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(playlist(3L, bohemian, stairway, backInBlack)));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponseDto result = service.removeSongFromPlaylist(3L, 1);

        assertThat(titles(result)).containsExactly("Bohemian Rhapsody", "Back in Black");
        assertThat(result.updatedAt()).isAfter(OLD_TIMESTAMP);
    }

    /**
     * Negative and out-of-range indexes raise {@code IllegalArgumentException} naming the index;
     * the playlist is not modified and nothing is saved.
     */
    @ParameterizedTest(name = "index {0} on a 3-song playlist")
    @ValueSource(ints = {-1, 3, 99})
    void invalidIndex_throwsAndLeavesPlaylistUntouched(int index) {
        Playlist existing = playlist(3L, bohemian, stairway, backInBlack);
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.removeSongFromPlaylist(3L, index))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid song index: " + index);
        assertThat(existing.getSongs()).containsExactly(bohemian, stairway, backInBlack);
        verify(playlistRepository, never()).save(any());
    }

    /**
     * Removing from a missing playlist raises {@code EntityNotFoundException}.
     */
    @Test
    void unknownPlaylist_throwsNotFound() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeSongFromPlaylist(9999L, 0))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
    }
}
