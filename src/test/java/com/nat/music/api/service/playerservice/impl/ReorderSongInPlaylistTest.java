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
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistSongManagerServiceImpl#reorderSongInPlaylist}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations.
 */
@ExtendWith(MockitoExtension.class)
class ReorderSongInPlaylistTest {

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
     * Moving a song forwards, backwards or onto its own position produces the expected order,
     * and {@code updatedAt} is refreshed.
     */
    @ParameterizedTest(name = "move {0} -> {1} gives {2}")
    @CsvSource({
            "0, 2, 'Stairway to Heaven|Back in Black|Bohemian Rhapsody'",
            "2, 0, 'Back in Black|Bohemian Rhapsody|Stairway to Heaven'",
            "1, 1, 'Bohemian Rhapsody|Stairway to Heaven|Back in Black'"
    })
    void movesSongAndRefreshesUpdatedAt(int from, int to, String expectedOrder) {
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(playlist(3L, bohemian, stairway, backInBlack)));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponseDto result = service.reorderSongInPlaylist(3L, from, to);

        assertThat(titles(result)).containsExactly(expectedOrder.split("\\|"));
        assertThat(result.updatedAt()).isAfter(OLD_TIMESTAMP);
    }

    /**
     * An invalid {@code fromIndex} or {@code toIndex} raises {@code IllegalArgumentException}
     * naming the faulty index; the playlist is not modified and nothing is saved.
     */
    @ParameterizedTest(name = "from {0} to {1} -> bad index {2}")
    @CsvSource({
            "-1, 0, -1",
            "3, 0, 3",
            "0, -1, -1",
            "0, 3, 3"
    })
    void invalidIndex_throwsAndLeavesPlaylistUntouched(int from, int to, int reportedIndex) {
        Playlist existing = playlist(3L, bohemian, stairway, backInBlack);
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.reorderSongInPlaylist(3L, from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid song index: " + reportedIndex);
        assertThat(existing.getSongs()).containsExactly(bohemian, stairway, backInBlack);
        verify(playlistRepository, never()).save(any());
    }

    /**
     * Reordering a missing playlist raises {@code EntityNotFoundException}.
     */
    @Test
    void unknownPlaylist_throwsNotFound() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reorderSongInPlaylist(9999L, 0, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
    }
}
