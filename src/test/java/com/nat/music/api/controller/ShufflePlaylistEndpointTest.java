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

import java.util.List;

import static com.nat.music.api.fixtures.DtoFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code GET /api/v1/playlists/{id}/shuffle}.
 * <p>
 * {@link PlaylistSongPremiumFeatureService} is mocked; strategy selection itself is covered by the service tests.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistService.class, PlaylistSongManagerService.class})
class ShufflePlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistSongPremiumFeatureService premiumFeatureService;

    /**
     * Without a {@code strategy} query parameter the controller asks the service for the
     * {@code RANDOM} strategy and returns the shuffled songs as-is.
     */
    @Test
    void withoutStrategyParam_usesRandom() throws Exception {
        when(premiumFeatureService.shufflePlaylist(1L, "RANDOM")).thenReturn(List.of(STAIRWAY, BOHEMIAN));

        mockMvc.perform(get(BASE_URL + "/1/shuffle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Stairway to Heaven"))
                .andExpect(jsonPath("$[1].title").value("Bohemian Rhapsody"));

        verify(premiumFeatureService).shufflePlaylist(1L, "RANDOM");
    }

    /**
     * The {@code strategy} query parameter is forwarded to the service unchanged.
     */
    @Test
    void passesStrategyParamToService() throws Exception {
        when(premiumFeatureService.shufflePlaylist(1L, "SMART")).thenReturn(List.of(BOHEMIAN, STAIRWAY));

        mockMvc.perform(get(BASE_URL + "/1/shuffle").param("strategy", "SMART"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(premiumFeatureService).shufflePlaylist(1L, "SMART");
    }

    /**
     * Shuffling a playlist that does not exist is answered with {@code 404}.
     */
    @Test
    void unknownPlaylist_returns404() throws Exception {
        when(premiumFeatureService.shufflePlaylist(9999L, "RANDOM"))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(get(BASE_URL + "/9999/shuffle"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }
}
