package com.nat.music.api.controller;


import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nat.music.api.fixtures.DtoFixtures.BASE_URL;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for errors handled by {@code GlobalExceptionHandler} that are not tied to a
 * single endpoint.
 */
@WebMvcTest(PlaylistManagerController.class)
@MockitoBean(types = {PlaylistSongManagerService.class, PlaylistSongPremiumFeatureService.class})
class GenericErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    /**
     * An HTTP method the resource does not support is answered with {@code 405}, an
     * {@code ErrorResponse} body and the {@code Allow} header listing the supported methods.
     */
    @Test
    void unsupportedMethod_returns405WithAllowHeader() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Request method 'PATCH' is not supported"));
    }

    /**
     * Any unhandled exception becomes a {@code 500} with a generic message; the original
     * exception message must never reach the client.
     */
    @Test
    void unexpectedException_returns500WithoutLeakingDetails() throws Exception {
        when(playlistService.getPlaylistById(1L)).thenThrow(new IllegalStateException("DB password is hunter2"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
