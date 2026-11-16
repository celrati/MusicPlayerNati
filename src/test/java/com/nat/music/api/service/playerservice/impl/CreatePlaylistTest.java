package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.nat.music.api.fixtures.EntityFixtures.newService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlaylistServiceImpl#createPlaylist}.
 * <p>
 * Repositories are Mockito mocks; the mappers are the real implementations so the returned
 * DTO is verified as well.
 */
@ExtendWith(MockitoExtension.class)
class CreatePlaylistTest {

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
     * The request is turned into a new, empty playlist that is saved once; the DTO returned
     * reflects the saved entity, including the id assigned by the repository.
     */
    @Test
    void savesNameAndDescriptionAndReturnsDto() {
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        PlaylistResponseDto result = service.createPlaylist(new PlaylistRequestDto("Road Trip", "Songs for the car"));

        ArgumentCaptor<Playlist> saved = ArgumentCaptor.forClass(Playlist.class);
        verify(playlistRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Road Trip");
        assertThat(saved.getValue().getDescription()).isEqualTo("Songs for the car");
        assertThat(saved.getValue().getSongs()).isEmpty();

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Road Trip");
        assertThat(result.songs()).isEmpty();
    }
}
