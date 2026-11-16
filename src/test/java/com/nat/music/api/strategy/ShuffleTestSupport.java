package com.nat.music.api.strategy;



import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;

import java.util.List;
import java.util.function.Function;

/** Small helpers shared by the shuffle strategy tests. */
final class ShuffleTestSupport {

    /** Number of shuffles per randomized test, enough to catch an ordering bug with near certainty. */
    static final int RUNS = 500;

    private static long nextId = 1;

    private ShuffleTestSupport() {
    }

    static Song song(String title, String artist, Genre genre) {
        return Song.builder().id(nextId++).title(title).artist(artist).genre(genre).duration(200).build();
    }

    /** Counts how many times two neighbours share the same key (artist, genre, ...). */
    static <K> int adjacentDuplicates(List<Song> songs, Function<Song, K> key) {
        int count = 0;
        for (int i = 1; i < songs.size(); i++) {
            if (key.apply(songs.get(i)).equals(key.apply(songs.get(i - 1)))) {
                count++;
            }
        }
        return count;
    }
}
