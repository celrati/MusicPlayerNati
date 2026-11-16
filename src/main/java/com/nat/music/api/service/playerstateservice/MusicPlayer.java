package com.nat.music.api.service.playerstateservice;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.model.PlayerState;

public interface MusicPlayer {

    void play(Song song);
    void pause();
    void stop();

    PlayerState getCurrentState();
}
