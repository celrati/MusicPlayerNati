package com.nat.music.api.controller;


import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static com.nat.music.api.fixtures.DtoFixtures.playlistDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code PUT /api/v1/playlists/{id}}.
 * <p>
 * {@link PlaylistService} is mocked; only routing, validation and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class UpdatePlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * A valid update is forwarded to the service with the path id and request body, and the
     * updated playlist is returned with {@code 200 OK}.
     */
    @Test
    void returns200WithUpdatedPlaylist() throws Exception {
        var request = new PlaylistRequestDto("Road Trip 2026", "Updated description");
        when(playlistService.updatePlaylistMetadata(3L, request))
                .thenReturn(playlistDto(3L, "Road Trip 2026", "Updated description"));

        mockMvc.perform(put(BASE_URL + "/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Road Trip 2026","description":"Updated description"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Road Trip 2026"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(playlistService).updatePlaylistMetadata(3L, request);
    }

    /**
     * Updating a playlist that does not exist is answered with {@code 404}.
     */
    @Test
    void unknownId_returns404() throws Exception {
        when(playlistService.updatePlaylistMetadata(9999L, new PlaylistRequestDto("x", "y")))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(put(BASE_URL + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"x","description":"y"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }

    /**
     * A name made only of whitespace fails {@code @NotBlank} validation and never reaches the service.
     */
    @Test
    void blankName_returns400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"   ","description":"y"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Playlist name cannot be blank"));

        verifyNoInteractions(playlistService);
    }
}
