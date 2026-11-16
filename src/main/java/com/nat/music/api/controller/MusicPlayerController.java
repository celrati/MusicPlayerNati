package com.nat.music.api.controller;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.model.PlayerState;
import com.nat.music.api.service.playerstateservice.MusicPlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/player")
@RequiredArgsConstructor
public class MusicPlayerController {

    private final MusicPlayer musicPlayer;

    /**
     * Récupérer l'état actuel du lecteur global (Chanson en cours + Statut)
     * GET /api/v1/player/status
     */
    @GetMapping("/status")
    public ResponseEntity<PlayerState> getStatus() {
        return ResponseEntity.ok(musicPlayer.getCurrentState());
    }

    /**
     * Lancer la lecture d'une chanson
     * POST /api/v1/player/play
     */
    @PostMapping("/play")
    public ResponseEntity<String> play(@RequestBody Song song) {
        musicPlayer.play(song);
        return ResponseEntity.ok("Now playing: " + song.getTitle());
    }

    /**
     * Mettre le lecteur en pause
     * POST /api/v1/player/pause
     */
    @PostMapping("/pause")
    public ResponseEntity<String> pause() {
        musicPlayer.pause();
        return ResponseEntity.ok("Player paused");
    }

    /**
     * Arrêter complètement le lecteur
     * POST /api/v1/player/stop
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        musicPlayer.stop();
        return ResponseEntity.ok("Player stopped");
    }
}
