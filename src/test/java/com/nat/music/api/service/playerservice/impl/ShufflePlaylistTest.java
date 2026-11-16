package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.dto.SongDto;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import com.nat.music.api.strategy.ShuffleStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistSongPremiumFeatureServiceImpl#shufflePlaylist}: strategy selection and side effects.
 * <p>
 * The strategies are Mockito mocks, so these tests check which strategy is used and what is
 * done with its result, not the shuffling algorithms themselves (see the strategy tests).
 */
@ExtendWith(MockitoExtension.class)
class ShufflePlaylistTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private ShuffleStrategy randomStrategy;
    @Mock
    private ShuffleStrategy smartStrategy;

    private PlaylistSongPremiumFeatureServiceImpl service;

    private final Song bohemian = bohemian();
    private final Song stairway = stairway();
    private final Song backInBlack = backInBlack();

    @BeforeEach
    void setUp() {
        service = newPremiumFeatureService(playlistRepository, songRepository,
                Map.of("RANDOM", randomStrategy, "SMART", smartStrategy));
    }

    /**
     * The strategy is looked up by name regardless of case; it receives the playlist's songs in
     * stored order and its result is returned as DTOs. No other strategy is used.
     */
    @ParameterizedTest
    @ValueSource(strings = {"SMART", "smart", "Smart"})
    void usesRequestedStrategyIgnoringCase(String strategyName) {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian, stairway, backInBlack)));
        when(smartStrategy.shuffle(anyList())).thenReturn(List.of(backInBlack, bohemian, stairway));

        List<SongDto> result = service.shufflePlaylist(1L, strategyName);

        assertThat(result).extracting(SongDto::title)
                .containsExactly("Back in Black", "Bohemian Rhapsody", "Stairway to Heaven");
        verify(smartStrategy).shuffle(List.of(bohemian, stairway, backInBlack));
        verifyNoInteractions(randomStrategy);
    }

    /**
     * A {@code null}, empty or unknown strategy name falls back to {@code RANDOM} instead of failing.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"UNKNOWN", ""})
    void nullOrUnknownStrategy_fallsBackToRandom(String strategyName) {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian, stairway)));
        when(randomStrategy.shuffle(anyList())).thenReturn(List.of(stairway, bohemian));

        List<SongDto> result = service.shufflePlaylist(1L, strategyName);

        assertThat(result).extracting(SongDto::title).containsExactly("Stairway to Heaven", "Bohemian Rhapsody");
        verifyNoInteractions(smartStrategy);
    }

    /**
     * Shuffling is read-only: the stored song order and {@code updatedAt} are unchanged and
     * nothing is saved.
     */
    @Test
    void doesNotSaveOrChangeThePlaylist() {
        Playlist existing = playlist(1L, bohemian, stairway);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(randomStrategy.shuffle(anyList())).thenReturn(List.of(stairway, bohemian));

        service.shufflePlaylist(1L, "RANDOM");

        assertThat(existing.getSongs()).containsExactly(bohemian, stairway);
        assertThat(existing.getUpdatedAt()).isEqualTo(OLD_TIMESTAMP);
        verify(playlistRepository, never()).save(any());
    }

    /**
     * Shuffling a missing playlist raises {@code EntityNotFoundException} before any strategy runs.
     */
    @Test
    void unknownPlaylist_throwsAndDoesNotShuffle() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.shufflePlaylist(9999L, "RANDOM"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
        verifyNoInteractions(randomStrategy, smartStrategy);
    }
}
