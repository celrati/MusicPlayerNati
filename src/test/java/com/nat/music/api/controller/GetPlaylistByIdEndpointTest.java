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

import static com.nat.music.api.fixtures.DtoFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code GET /api/v1/playlists/{id}}.
 * <p>
 * {@link PlaylistService} is mocked; only routing, JSON serialization and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class GetPlaylistByIdEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * An existing playlist is returned with {@code 200 OK}, including its songs in order
     * and their fields (title, artist, genre).
     */
    @Test
    void returns200WithSongs() throws Exception {
        when(playlistService.getPlaylistById(1L))
                .thenReturn(playlistDto(1L, "70s Classic Rock", "Best of the 70s", BOHEMIAN, STAIRWAY));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("70s Classic Rock"))
                .andExpect(jsonPath("$.songs", hasSize(2)))
                .andExpect(jsonPath("$.songs[0].title").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$.songs[0].genre").value("ROCK"))
                .andExpect(jsonPath("$.songs[1].artist").value("Led Zeppelin"));
    }

    /**
     * An {@code EntityNotFoundException} from the service is mapped to a {@code 404}
     * {@code ErrorResponse}; the {@code errors} map is omitted because it is not a validation error.
     */
    @Test
    void unknownId_returns404() throws Exception {
        when(playlistService.getPlaylistById(9999L))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(get(BASE_URL + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/9999"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    /**
     * A path id that cannot be converted to {@code Long} is rejected with {@code 400}
     * without calling the service.
     */
    @Test
    void nonNumericId_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to convert 'id' with value: 'abc'"));

        verifyNoInteractions(playlistService);
    }
}
