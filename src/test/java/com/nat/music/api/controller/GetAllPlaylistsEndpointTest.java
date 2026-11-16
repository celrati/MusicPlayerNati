package com.nat.music.api.controller;

import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.nat.music.api.fixtures.DtoFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code GET /api/v1/playlists}.
 * <p>
 * {@link PlaylistService} is mocked; only routing and JSON serialization are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class GetAllPlaylistsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * Every playlist returned by the service is serialized, in the same order, with {@code 200 OK}.
     */
    @Test
    void returns200WithAllPlaylists() throws Exception {
        when(playlistService.getAllPlaylists()).thenReturn(List.of(
                playlistDto(1L, "70s Classic Rock", "d1", BOHEMIAN),
                playlistDto(2L, "Guitar Legends", "d2", STAIRWAY)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("70s Classic Rock"))
                .andExpect(jsonPath("$[1].name").value("Guitar Legends"));
    }

    /**
     * When there are no playlists the endpoint still answers {@code 200 OK} with an empty JSON
     * array, not {@code 404} or {@code null}.
     */
    @Test
    void noPlaylists_returns200WithEmptyArray() throws Exception {
        when(playlistService.getAllPlaylists()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
