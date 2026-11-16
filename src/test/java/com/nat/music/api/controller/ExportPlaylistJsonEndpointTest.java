package com.nat.music.api.controller;

import com.nat.music.api.service.playerservice.PlaylistExportService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@code GET /api/v1/playlists/{id}/export/json}.
 * <p>
 * {@link PlaylistExportService} is mocked; only routing, JSON serialization and error mapping are tested.
 */
@WebMvcTest(PlaylistExportController.class)
class ExportPlaylistJsonEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistExportService exportService;

    /**
     * An existing playlist is serialized as JSON with {@code 200 OK}, songs in order.
     */
    @Test
    void returns200WithPlaylistJson() throws Exception {
        when(exportService.getPlaylistOrThrow(1L)).thenReturn(playlist(1L, bohemian(), stairway()));

        mockMvc.perform(get(BASE_URL + "/1/export/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Road Trip"))
                .andExpect(jsonPath("$.description").value("Songs for the car"))
                .andExpect(jsonPath("$.songs", hasSize(2)))
                .andExpect(jsonPath("$.songs[0].title").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$.songs[1].artist").value("Led Zeppelin"));
    }

    /**
     * An {@code EntityNotFoundException} from the service is mapped to a {@code 404} {@code ErrorResponse}.
     */
    @Test
    void unknownId_returns404() throws Exception {
        when(exportService.getPlaylistOrThrow(9999L))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(get(BASE_URL + "/9999/export/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/9999/export/json"));
    }

    /**
     * A non-numeric id is rejected with {@code 400} without calling the service.
     */
    @Test
    void nonNumericId_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc/export/json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exportService);
    }
}
