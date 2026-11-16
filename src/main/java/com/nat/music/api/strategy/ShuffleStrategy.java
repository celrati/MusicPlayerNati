package com.nat.music.api.strategy;


import com.nat.music.api.domain.entity.Song;

import java.util.List;

public interface ShuffleStrategy {

    List<Song> shuffle(List<Song> songs);
}