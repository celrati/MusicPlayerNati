package com.nat.music.api.controller;

import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static com.nat.music.api.fixtures.DtoFixtures.playlistDto;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code POST /api/v1/playlists}.
 * <p>
 * Runs only the MVC slice ({@link PlaylistManagerController}, validation and
 * {@code GlobalExceptionHandler}); {@link PlaylistService} is replaced by a Mockito mock,
 * so no database is involved.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class CreatePlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * A valid request is forwarded to the service unchanged and answered with
     * {@code 201 Created} and the created playlist serialized as JSON.
     */
    @Test
    void returns201WithCreatedPlaylist() throws Exception {
        var request = new PlaylistRequestDto("Road Trip", "Songs for the car");
        when(playlistService.createPlaylist(request)).thenReturn(playlistDto(3L, "Road Trip", "Songs for the car"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Road Trip","description":"Songs for the car"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Road Trip"))
                .andExpect(jsonPath("$.description").value("Songs for the car"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$.songs", hasSize(0)));

        verify(playlistService).createPlaylist(request);
    }

    /**
     * An empty {@code name} is rejected by bean validation: the response is a {@code 400}
     * {@code ErrorResponse} listing the offending field, and the service is never called.
     */
    @Test
    void blankName_returns400WithFieldError() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":"blank name"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(BASE_URL))
                .andExpect(jsonPath("$.errors.name").value("Playlist name cannot be blank"));

        verifyNoInteractions(playlistService);
    }

    /**
     * A request without any {@code name} property fails validation the same way as a blank one.
     */
    @Test
    void missingName_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"no name"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Playlist name cannot be blank"));

        verifyNoInteractions(playlistService);
    }

    /**
     * A body that is not valid JSON is rejected with {@code 400} before reaching the service.
     */
    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": broken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to read request"));

        verifyNoInteractions(playlistService);
    }
}
