package com.nat.music.api.controller;


import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code DELETE /api/v1/playlists/{playlistId}/songs/index/{index}}.
 * <p>
 * {@link PlaylistSongManagerService} is mocked; only routing and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistService.class, PlaylistSongPremiumFeatureService.class})
class RemoveSongFromPlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistSongManagerService playlistSongManagerService;

    /**
     * Removing a song by index returns {@code 200 OK} with the updated playlist, and both path
     * variables are passed to the service.
     */
    @Test
    void returns200WithSongRemoved() throws Exception {
        when(playlistSongManagerService.removeSongFromPlaylist(3L, 1)).thenReturn(playlistDto(3L, "Road Trip", "d", BOHEMIAN));

        mockMvc.perform(delete(BASE_URL + "/3/songs/index/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs", hasSize(1)));

        verify(playlistSongManagerService).removeSongFromPlaylist(3L, 1);
    }

    /**
     * An {@code IllegalArgumentException} raised for an out-of-range index is mapped to {@code 400}.
     */
    @Test
    void invalidIndex_returns400() throws Exception {
        when(playlistSongManagerService.removeSongFromPlaylist(1L, 99))
                .thenThrow(new IllegalArgumentException("Invalid song index: 99"));

        mockMvc.perform(delete(BASE_URL + "/1/songs/index/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid song index: 99"));
    }
}
