package com.nat.music.api.strategy.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.strategy.ShuffleStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("SMART")
public class SmartShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<Song> shuffle(List<Song> songs) {
        if(songs == null) {
            return Collections.emptyList();
        }

        if (songs.size() <= 2) {
            return new ArrayList<>(songs);
        }

        List<Song> pool = new ArrayList<>(songs);
        // starting shuffle..
        Collections.shuffle(pool);

        List<Song> result = new ArrayList<>();
        result.add(pool.remove(0));

        while (!pool.isEmpty()) {
            Song lastAdded = result.get(result.size() - 1);
            Song nextSong = null;

            // search a different song from the previous artist in the result list..
            for (int i = 0; i < pool.size(); i++) {
                if (!pool.get(i).getArtist().equalsIgnoreCase(lastAdded.getArtist())) {
                    nextSong = pool.remove(i);
                    break;
                }
            }

            // otherwise we take the first song..
            if (nextSong == null) {
                nextSong = pool.remove(0);
            }

            result.add(nextSong);
        }

        return result;
    }
}