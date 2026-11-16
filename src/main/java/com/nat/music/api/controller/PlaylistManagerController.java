package com.nat.music.api.controller;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.dto.ReorderRequestDto;
import com.nat.music.api.dto.SongDto;
import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistManagerController {

    private final PlaylistService playlistService;
    private final PlaylistSongManagerService playlistSongManagerService;
    private final PlaylistSongPremiumFeatureService playlistSongPremiumFeatureService;

    @PostMapping
    public ResponseEntity<PlaylistResponseDto> createPlaylist(@Valid @RequestBody PlaylistRequestDto request) {
        PlaylistResponseDto created = playlistService.createPlaylist(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDto> getPlaylistById(@PathVariable Long id) {
        var playlist = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(playlist);
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDto>> getAllPlaylists() {
        var playlists = playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponseDto> updatePlaylistMetadata(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistRequestDto request) {
        var updatedPlaylist = playlistService.updatePlaylistMetadata(id, request);
        return ResponseEntity.ok(updatedPlaylist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<PlaylistResponseDto> addSongToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        return ResponseEntity.ok(playlistSongManagerService.addSongToPlaylist(playlistId, songId));
    }

    @DeleteMapping("/{playlistId}/songs/index/{index}")
    public ResponseEntity<PlaylistResponseDto> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable int index) {
        return ResponseEntity.ok(playlistSongManagerService.removeSongFromPlaylist(playlistId, index));
    }

    @PutMapping("/{playlistId}/songs/reorder")
    public ResponseEntity<PlaylistResponseDto> reorderSongInPlaylist(
            @PathVariable Long playlistId,
            @Valid @RequestBody ReorderRequestDto request) {
        return ResponseEntity.ok(playlistSongManagerService.reorderSongInPlaylist(playlistId, request.fromIndex(), request.toIndex()));
    }

    @GetMapping("/{id}/shuffle")
    public ResponseEntity<List<SongDto>> shufflePlaylist(
            @PathVariable Long id,
            @RequestParam(defaultValue = "RANDOM") String strategy) {

        List<SongDto> shuffledSongs = playlistSongPremiumFeatureService.shufflePlaylist(id, strategy);
        return ResponseEntity.ok(shuffledSongs);
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<Song>> getRecommendations(@PathVariable Long id) {
        List<Song> recommendedSongs = playlistSongPremiumFeatureService.getRecommendations(id);
        return ResponseEntity.ok(recommendedSongs);
    }
}
