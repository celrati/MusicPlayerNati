package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.nat.music.api.fixtures.EntityFixtures.newService;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistServiceImpl#deletePlaylist}.
 * <p>
 * The repository is a Mockito mock.
 */
@ExtendWith(MockitoExtension.class)
class DeletePlaylistTest {

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
     * An existing playlist is deleted by id.
     */
    @Test
    void existingId_deletes() {
        when(playlistRepository.existsById(3L)).thenReturn(true);

        service.deletePlaylist(3L);

        verify(playlistRepository).deleteById(3L);
    }

    /**
     * Deleting a missing playlist raises {@code EntityNotFoundException} and never calls
     * {@code deleteById}.
     */
    @Test
    void unknownId_throwsAndDoesNotDelete() {
        when(playlistRepository.existsById(9999L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePlaylist(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
        verify(playlistRepository, never()).deleteById(any());
    }
}
