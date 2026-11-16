package com.nat.music.api.strategy.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.strategy.ShuffleStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("GENRE_BALANCED")
public class GenreBalancedShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<Song> shuffle(List<Song> songs) {
        if(songs == null) {
            return Collections.emptyList();
        }

        if (songs.size() <= 2) {
            return new ArrayList<>(songs);
        }


        // group by genre in the hasjmap..
        Map<Genre, List<Song>> genreMap = songs.stream()
                .collect(Collectors.groupingBy(Song::getGenre));

        // random shuffle
        for (List<Song> genreList : genreMap.values()) {
            Collections.shuffle(genreList);
        }

        List<Song> result = new ArrayList<>();
        List<List<Song>> buckets = new ArrayList<>(genreMap.values());

        // loop on each bucket one after the other
        // A A A
        // B B B
        // C C C

        // result -> A B C A B C A B C
        boolean added;
        do {
            added = false;
            for (List<Song> bucket : buckets) {
                if (!bucket.isEmpty()) {
                    result.add(bucket.remove(0));
                    added = true;
                }
            }
        } while (added);

        return result;
    }
}