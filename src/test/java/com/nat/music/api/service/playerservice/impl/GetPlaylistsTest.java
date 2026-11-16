package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlaylistServiceImpl#getPlaylistById} and
 * {@link PlaylistServiceImpl#getAllPlaylists}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations.
 */
@ExtendWith(MockitoExtension.class)
class GetPlaylistsTest {

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
     * An existing playlist is mapped to a DTO with its songs in their stored order.
     */
    @Test
    void getById_returnsMappedPlaylist() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian(), stairway())));

        PlaylistResponseDto result = service.getPlaylistById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Road Trip");
        assertThat(titles(result)).containsExactly("Bohemian Rhapsody", "Stairway to Heaven");
    }

    /**
     * A missing playlist raises {@code EntityNotFoundException} with a message containing the id.
     */
    @Test
    void getById_unknownId_throwsNotFound() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlaylistById(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
    }

    /**
     * Every playlist from the repository is mapped, keeping the repository order.
     */
    @Test
    void getAll_returnsEveryPlaylistMapped() {
        when(playlistRepository.findAll()).thenReturn(List.of(playlist(1L, bohemian()), playlist(2L)));

        List<PlaylistResponseDto> result = service.getAllPlaylists();

        assertThat(result).extracting(PlaylistResponseDto::id).containsExactly(1L, 2L);
        assertThat(result.get(0).songs()).hasSize(1);
    }

    /**
     * An empty repository yields an empty list, never {@code null}.
     */
    @Test
    void getAll_noPlaylists_returnsEmptyList() {
        when(playlistRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllPlaylists()).isEmpty();
    }
}
