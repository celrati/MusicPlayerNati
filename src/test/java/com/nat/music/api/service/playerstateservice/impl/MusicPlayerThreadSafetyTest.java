package com.nat.music.api.service.playerstateservice.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.service.playerstateservice.MusicPlayer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
class MusicPlayerThreadSafetyTest {

    private static final int THREADS = 50;

    @Test
    void testConcurrentPlayAndRead() throws InterruptedException {

        MusicPlayer playerService = new MusicPlayerImpl();

        log.info("=== DÉBUT DU TEST DE CONCURRENCE : {} threads initialisés ===", THREADS);

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            final long songId = i;
            executorService.submit(() -> {
                try {

                    startLatch.await(); // wait to start for each thread

                    Song song = new Song();
                    song.setId(songId);
                    song.setTitle("Song " + songId);

                    if (songId % 2 == 0) {
                        log.info("Thread [ID: {}] -> Action: PLAY Song {}", songId, songId);
                        playerService.play(song);
                    } else {
                        log.info("Thread [ID: {}] -> Action: PAUSE", songId);
                        playerService.pause();
                    }

                    var state = playerService.getCurrentState();
                    assertNotNull(state);

                } catch (InterruptedException e) {
                    log.error("Thread interrompu", e);
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown(); // signal that a thread is finihed
                }
            });
        }

        log.info(">>> TOP DÉPART : Lâcher des threads simultané !");
        startLatch.countDown(); // start all threads at once


        finishLatch.await(); // wait for all threads
        executorService.shutdown();

        log.info("=== FIN DU TEST : Tous les threads ont terminé. État final du lecteur : {} ===", playerService.getCurrentState());

        // Vérification finale
        assertNotNull(playerService.getCurrentState());
    }
}