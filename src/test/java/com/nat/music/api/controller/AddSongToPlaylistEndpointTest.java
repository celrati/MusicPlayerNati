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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code POST /api/v1/playlists/{playlistId}/songs/{songId}}.
 * <p>
 * {@link PlaylistSongManagerService} is mocked; only routing and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistService.class, PlaylistSongPremiumFeatureService.class})
class AddSongToPlaylistEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistSongManagerService playlistSongManagerService;

    /**
     * Adding a song returns {@code 200 OK} with the updated playlist containing the new song.
     */
    @Test
    void returns200WithSongAdded() throws Exception {
        when(playlistSongManagerService.addSongToPlaylist(3L, 1L)).thenReturn(playlistDto(3L, "Road Trip", "d", BOHEMIAN));

        mockMvc.perform(post(BASE_URL + "/3/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs", hasSize(1)))
                .andExpect(jsonPath("$.songs[0].id").value(1))
                .andExpect(jsonPath("$.songs[0].title").value("Bohemian Rhapsody"));
    }

    /**
     * An unknown song id is answered with {@code 404} and a message naming the song.
     */
    @Test
    void unknownSong_returns404() throws Exception {
        when(playlistSongManagerService.addSongToPlaylist(1L, 9999L))
                .thenThrow(new EntityNotFoundException("Song not found with id: 9999"));

        mockMvc.perform(post(BASE_URL + "/1/songs/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with id: 9999"));
    }

    /**
     * An unknown playlist id is answered with {@code 404} and a message naming the playlist.
     */
    @Test
    void unknownPlaylist_returns404() throws Exception {
        when(playlistSongManagerService.addSongToPlaylist(9999L, 1L))
                .thenThrow(new EntityNotFoundException("Playlist not found with id: 9999"));

        mockMvc.perform(post(BASE_URL + "/9999/songs/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with id: 9999"));
    }
}
