package com.nat.music.api.controller;


import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@code DELETE /api/v1/playlists/{id}}.
 * <p>
 * {@link PlaylistService} is mocked; only routing, status codes and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class DeletePlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * A successful delete is answered with {@code 204 No Content} and no body, and the service
     * receives the id from the path.
     */
    @Test
    void returns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(playlistService).deletePlaylist(3L);
    }

    /**
     * Deleting a playlist that does not exist is answered with {@code 404}.
     */
    @Test
    void unknownId_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Playlist not found with id: 9999"))
                .when(playlistService).deletePlaylist(9999L);

        mockMvc.perform(delete(BASE_URL + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }
}
