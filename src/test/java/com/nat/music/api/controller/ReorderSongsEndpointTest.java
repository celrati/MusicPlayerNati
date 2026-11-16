package com.nat.music.api.controller;


import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code PUT /api/v1/playlists/{playlistId}/songs/reorder}.
 * <p>
 * {@link PlaylistSongManagerService} is mocked; only routing, validation and error mapping are tested.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistService.class, PlaylistSongPremiumFeatureService.class})
class ReorderSongsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistSongManagerService playlistSongManagerService;

    /**
     * The {@code fromIndex}/{@code toIndex} body fields are passed to the service and the
     * reordered playlist is returned with {@code 200 OK}.
     */
    @Test
    void returns200AndPassesIndexesToService() throws Exception {
        when(playlistSongManagerService.reorderSongInPlaylist(3L, 0, 1))
                .thenReturn(playlistDto(3L, "Road Trip", "d", STAIRWAY, BOHEMIAN));

        mockMvc.perform(put(BASE_URL + "/3/songs/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromIndex":0,"toIndex":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs[0].title").value("Stairway to Heaven"))
                .andExpect(jsonPath("$.songs[1].title").value("Bohemian Rhapsody"));

        verify(playlistSongManagerService).reorderSongInPlaylist(3L, 0, 1);
    }

    /**
     * Negative indexes fail {@code @Min(0)} validation; every invalid field is reported in the
     * {@code errors} map and the service is never called.
     */
    @Test
    void negativeIndexes_returns400WithBothFieldErrors() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/songs/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromIndex":-1,"toIndex":-2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fromIndex").value("fromIndex must be greater than or equal to 0"))
                .andExpect(jsonPath("$.errors.toIndex").value("toIndex must be greater than or equal to 0"));

        verifyNoInteractions(playlistSongManagerService);
    }

    /**
     * An index beyond the playlist size passes validation but is rejected by the service; the
     * resulting {@code IllegalArgumentException} is mapped to {@code 400}.
     */
    @Test
    void indexOutOfRange_returns400() throws Exception {
        when(playlistSongManagerService.reorderSongInPlaylist(anyLong(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid song index: 99"));

        mockMvc.perform(put(BASE_URL + "/1/songs/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromIndex":0,"toIndex":99}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid song index: 99"));
    }
}
