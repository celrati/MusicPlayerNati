package com.nat.music.api.controller;



import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.service.playerservice.PlaylistExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistExportController {

    private final PlaylistExportService exportService;

    @GetMapping("/{id}/export/json")
    public ResponseEntity<Playlist> exportJson(@PathVariable Long id) {
        Playlist playlist = exportService.getPlaylistOrThrow(id);
        return ResponseEntity.ok(playlist);
    }

    /**
     * Export en M3U (fichier texte téléchargeable)
     * URL : GET /api/v1/playlists/1/export/m3u
     */
    @GetMapping("/{id}/export/m3u")
    public ResponseEntity<String> exportM3U(@PathVariable Long id) {
        String m3uContent = exportService.exportToM3U(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=playlist-" + id + ".m3u");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(m3uContent);
    }
}
