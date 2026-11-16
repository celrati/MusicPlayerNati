package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistServiceImpl#updatePlaylistMetadata}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations.
 */
@ExtendWith(MockitoExtension.class)
class UpdatePlaylistMetadataTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;

    private PlaylistServiceImpl service;

    @BeforeEach
    void setUp() {
        service = newService(playlistRepository, songRepository, Map.of());
    }

    /**
     * Name and description are replaced, {@code updatedAt} is refreshed while {@code createdAt}
     * and the songs are left untouched, and the playlist is saved.
     */
    @Test
    void updatesFieldsAndRefreshesUpdatedAt() {
        Playlist existing = playlist(3L, bohemian());
        when(playlistRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponseDto result =
                service.updatePlaylistMetadata(3L, new PlaylistRequestDto("Road Trip 2026", "Updated"));

        assertThat(result.name()).isEqualTo("Road Trip 2026");
        assertThat(result.description()).isEqualTo("Updated");
        assertThat(result.updatedAt()).isAfter(OLD_TIMESTAMP);
        assertThat(result.createdAt()).isEqualTo(OLD_TIMESTAMP);
        assertThat(titles(result)).containsExactly("Bohemian Rhapsody");
        verify(playlistRepository).save(existing);
    }

    /**
     * Updating a missing playlist raises {@code EntityNotFoundException} and saves nothing.
     */
    @Test
    void unknownId_throwsAndDoesNotSave() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePlaylistMetadata(9999L, new PlaylistRequestDto("x", "y")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
        verify(playlistRepository, never()).save(any());
    }
}
