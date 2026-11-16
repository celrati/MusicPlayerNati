package com.nat.music.api.strategy;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.strategy.impl.RandomShuffleStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nat.music.api.strategy.ShuffleTestSupport.RUNS;
import static com.nat.music.api.strategy.ShuffleTestSupport.song;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RandomShuffleStrategy}.
 * <p>
 * Randomized properties are checked over {@code RUNS} shuffles so that an ordering bug is
 * caught with near certainty.
 */
class RandomShuffleStrategyTest {

    private final RandomShuffleStrategy strategy = new RandomShuffleStrategy();

    private final List<Song> songs = List.of(
            song("Bohemian Rhapsody", "Queen", Genre.ROCK),
            song("Stairway to Heaven", "Led Zeppelin", Genre.ROCK),
            song("Comfortably Numb", "Pink Floyd", Genre.ROCK),
            song("Paint It Black", "The Rolling Stones", Genre.ROCK),
            song("Hotel California", "Eagles", Genre.ROCK));

    /**
     * Every shuffle returns the same songs as the input: none lost, duplicated or added.
     */
    @Test
    void returnsExactlyTheSameSongs() {
        for (int i = 0; i < RUNS; i++) {
            assertThat(strategy.shuffle(songs)).containsExactlyInAnyOrderElementsOf(songs);
        }
    }

    /**
     * The input list is left in its original order; the strategy works on a copy.
     */
    @Test
    void doesNotModifyTheInputList() {
        List<Song> input = new java.util.ArrayList<>(songs);

        strategy.shuffle(input);

        assertThat(input).containsExactlyElementsOf(songs);
    }

    /**
     * Repeated shuffles produce many different orders, proving the output is actually shuffled.
     */
    @Test
    void producesDifferentOrders() {
        Set<List<Song>> orders = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            orders.add(strategy.shuffle(songs));
        }
        // 120 possible orders for 5 songs: a real shuffle sees many of them
        assertThat(orders).hasSizeGreaterThan(50);
    }

    /**
     * Each song appears in first position at least once, a basic check that the shuffle is not biased.
     */
    @Test
    void everySongCanEndUpFirst() {
        Set<Song> firstSongs = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            firstSongs.add(strategy.shuffle(songs).get(0));
        }
        assertThat(firstSongs).containsExactlyInAnyOrderElementsOf(songs);
    }

    /**
     * Shuffling an empty list returns an empty list.
     */
    @Test
    void emptyList_returnsEmptyList() {
        assertThat(strategy.shuffle(List.of())).isEmpty();
    }

    /**
     * Shuffling a single song returns that song.
     */
    @Test
    void singleSong_returnsThatSong() {
        Song only = songs.get(0);
        assertThat(strategy.shuffle(List.of(only))).containsExactly(only);
    }
}
