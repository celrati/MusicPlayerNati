package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.repository.PlaylistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlaylistExportServiceImpl#exportToM3U}.
 */
@ExtendWith(MockitoExtension.class)
class ExportToM3UTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private PlaylistExportServiceImpl service;

    /**
     * Each song produces an {@code #EXTINF} line (duration, artist - title) followed by its file path,
     * in playlist order, after the {@code #EXTM3U} and {@code #PLAYLIST} headers.
     */
    @Test
    void writesHeaderAndOneEntryPerSongInOrder() {
        Song first = bohemian();
        first.setFilePath("/music/bohemian.mp3");
        Song second = stairway();
        second.setFilePath("/music/stairway.mp3");
        second.setDuration(482);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, first, second)));

        String m3u = service.exportToM3U(1L);

        assertThat(m3u).isEqualTo("""
                #EXTM3U
                #PLAYLIST:Road Trip
                #EXTINF:300,Queen - Bohemian Rhapsody
                /music/bohemian.mp3
                #EXTINF:482,Led Zeppelin - Stairway to Heaven
                /music/stairway.mp3
                """);
    }

    /**
     * A song without a file path is written with the {@code unknown-path} placeholder.
     */
    @Test
    void missingFilePath_usesUnknownPathPlaceholder() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian())));

        String m3u = service.exportToM3U(1L);

        assertThat(m3u).endsWith("#EXTINF:300,Queen - Bohemian Rhapsody\nunknown-path\n");
    }

    /**
     * An empty playlist yields only the two header lines.
     */
    @Test
    void emptyPlaylist_writesOnlyHeaders() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L)));

        assertThat(service.exportToM3U(1L)).isEqualTo("#EXTM3U\n#PLAYLIST:Road Trip\n");
    }

    /**
     * A missing playlist raises {@code EntityNotFoundException}.
     */
    @Test
    void unknownId_throwsNotFound() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exportToM3U(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
    }
}
