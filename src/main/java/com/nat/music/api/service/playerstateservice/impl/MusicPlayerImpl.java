package com.nat.music.api.service.playerstateservice.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.PlayerStatus;
import com.nat.music.api.domain.model.PlayerState;
import com.nat.music.api.service.playerstateservice.MusicPlayer;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class MusicPlayerImpl implements MusicPlayer {

    private Song currentSong = null;
    private PlayerStatus status = PlayerStatus.STOPPED;


    private final ReentrantLock lock = new ReentrantLock();

    public void play(Song song) {
        lock.lock();
        try {
            this.currentSong = song;
            this.status = PlayerStatus.PLAYING;
        } finally {
            lock.unlock();
        }
    }

    public void pause() {
        lock.lock();
        try {
            if (PlayerStatus.PLAYING.equals(this.status)) {
                this.status = PlayerStatus.PAUSED;
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            this.status = PlayerStatus.STOPPED;
            this.currentSong = null;
        } finally {
            lock.unlock();
        }
    }

    public PlayerState getCurrentState() {
        lock.lock();
        try {
            return new PlayerState(currentSong, status);
        } finally {
            lock.unlock();
        }
    }

}
