package com.nat.music.api.controller;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
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

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static com.nat.music.api.fixtures.EntityFixtures.song;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code GET /api/v1/playlists/{id}/recommendations}.
 * <p>
 * {@link PlaylistSongPremiumFeatureService} is mocked; how recommendations are chosen is covered by the service tests.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistService.class, PlaylistSongManagerService.class})
class GetRecommendationsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistSongPremiumFeatureService premiumFeatureService;

    /**
     * The recommended songs are returned as a JSON array with their fields.
     */
    @Test
    void returnsRecommendedSongs() throws Exception {
        Song paintItBlack = song(5L, "Paint It Black", "The Rolling Stones", Genre.ROCK);
        Song soWhat = song(10L, "So What", "Miles Davis", Genre.JAZZ);
        when(premiumFeatureService.getRecommendations(1L)).thenReturn(List.of(paintItBlack, soWhat));

        mockMvc.perform(get(BASE_URL + "/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("Paint It Black"))
                .andExpect(jsonPath("$[0].artist").value("The Rolling Stones"))
                .andExpect(jsonPath("$[0].genre").value("ROCK"))
                .andExpect(jsonPath("$[1].title").value("So What"))
                .andExpect(jsonPath("$[1].genre").value("JAZZ"));
    }

    /**
     * No recommendation is answered with {@code 200} and an empty array, not an error.
     */
    @Test
    void noRecommendations_returnsEmptyArray() throws Exception {
        when(premiumFeatureService.getRecommendations(1L)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Asking recommendations for a playlist that does not exist is answered with {@code 404}.
     */
    @Test
    void unknownPlaylist_returns404() throws Exception {
        when(premiumFeatureService.getRecommendations(9999L))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(get(BASE_URL + "/9999/recommendations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }
}
