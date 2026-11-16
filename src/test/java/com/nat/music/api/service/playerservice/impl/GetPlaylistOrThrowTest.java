package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.repository.PlaylistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.bohemian;
import static com.nat.music.api.fixtures.EntityFixtures.playlist;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlaylistExportServiceImpl#getPlaylistOrThrow}.
 */
@ExtendWith(MockitoExtension.class)
class GetPlaylistOrThrowTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistExportServiceImpl service;

    /**
     * An existing playlist is returned as-is from the repository.
     */
    @Test
    void existingId_returnsPlaylist() {
        Playlist playlist = playlist(1L, bohemian());
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        assertThat(service.getPlaylistOrThrow(1L)).isSameAs(playlist);
    }

    /**
     * A missing playlist raises {@code EntityNotFoundException} with a message containing the id.
     */
    @Test
    void unknownId_throwsNotFound() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlaylistOrThrow(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
    }
}
