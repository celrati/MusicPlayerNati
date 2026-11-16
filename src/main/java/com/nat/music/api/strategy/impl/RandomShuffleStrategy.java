package com.nat.music.api.strategy.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.strategy.ShuffleStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("RANDOM")
public class RandomShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<Song> shuffle(List<Song> songs) {
        List<Song> shuffledList = new ArrayList<>(songs);
        Collections.shuffle(shuffledList);
        return shuffledList;
    }
}