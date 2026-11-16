package com.nat.music.api.controller;

import com.nat.music.api.service.playerservice.PlaylistExportService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@code GET /api/v1/playlists/{id}/export/m3u}.
 * <p>
 * {@link PlaylistExportService} is mocked; only routing, headers, content type and error mapping are tested.
 */
@WebMvcTest(PlaylistExportController.class)
class ExportPlaylistM3UEndpointTest {

    private static final String M3U = "#EXTM3U\n#PLAYLIST:Road Trip\n#EXTINF:300,Queen - Bohemian Rhapsody\n/music/bohemian.mp3\n";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistExportService exportService;

    /**
     * The M3U content is returned as plain text with an attachment header naming the file after the id.
     */
    @Test
    void returns200WithM3UAttachment() throws Exception {
        when(exportService.exportToM3U(1L)).thenReturn(M3U);

        mockMvc.perform(get(BASE_URL + "/1/export/m3u"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=playlist-1.m3u"))
                .andExpect(content().string(M3U));
    }

    /**
     * An {@code EntityNotFoundException} from the service is mapped to a {@code 404} {@code ErrorResponse}.
     */
    @Test
    void unknownId_returns404() throws Exception {
        when(exportService.exportToM3U(9999L))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(get(BASE_URL + "/9999/export/m3u"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }

    /**
     * A non-numeric id is rejected with {@code 400} without calling the service.
     */
    @Test
    void nonNumericId_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc/export/m3u"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exportService);
    }
}
