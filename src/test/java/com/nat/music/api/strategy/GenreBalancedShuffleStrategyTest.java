package com.nat.music.api.strategy;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.strategy.impl.GenreBalancedShuffleStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nat.music.api.strategy.ShuffleTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GenreBalancedShuffleStrategy}, which interleaves genres.
 * <p>
 * Randomized properties are checked over {@code RUNS} shuffles.
 */
class GenreBalancedShuffleStrategyTest {

    private final GenreBalancedShuffleStrategy strategy = new GenreBalancedShuffleStrategy();

    /** 3 ROCK + 3 POP + 3 JAZZ. */
    private final List<Song> balanced = List.of(
            song("Rock 1", "Band A", Genre.ROCK),
            song("Rock 2", "Band B", Genre.ROCK),
            song("Rock 3", "Band C", Genre.ROCK),
            song("Pop 1", "Singer A", Genre.POP),
            song("Pop 2", "Singer B", Genre.POP),
            song("Pop 3", "Singer C", Genre.POP),
            song("Jazz 1", "Trio A", Genre.JAZZ),
            song("Jazz 2", "Trio B", Genre.JAZZ),
            song("Jazz 3", "Trio C", Genre.JAZZ));

    /**
     * Every shuffle returns the same songs as the input: none lost, duplicated or added.
     */
    @Test
    void returnsExactlyTheSameSongs() {
        for (int i = 0; i < RUNS; i++) {
            assertThat(strategy.shuffle(balanced)).containsExactlyInAnyOrderElementsOf(balanced);
        }
    }

    /**
     * With genres of equal size, songs are interleaved so that no two consecutive songs share a genre.
     */
    @Test
    void neverPlaysTheSameGenreTwiceInARow_whenGenresHaveTheSameSize() {
        for (int i = 0; i < RUNS; i++) {
            List<Song> result = strategy.shuffle(balanced);
            assertThat(adjacentDuplicates(result, Song::getGenre))
                    .as("same genre back to back in %s", result.stream().map(Song::getTitle).toList())
                    .isZero();
        }
    }

    /**
     * Songs are shuffled inside each genre, so the output is not always the same order.
     */
    @Test
    void shufflesSongsWithinEachGenre() {
        Set<List<Song>> orders = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            orders.add(strategy.shuffle(balanced));
        }
        assertThat(orders).hasSizeGreaterThan(10);
    }

    /**
     * With a single genre there is nothing to interleave and all songs are still returned.
     */
    @Test
    void singleGenre_returnsAllSongs() {
        List<Song> rockOnly = balanced.subList(0, 3);

        assertThat(strategy.shuffle(rockOnly)).containsExactlyInAnyOrderElementsOf(rockOnly);
    }

    /**
     * The input list is left in its original order; the strategy works on a copy.
     */
    @Test
    void doesNotModifyTheInputList() {
        List<Song> input = new ArrayList<>(balanced);

        strategy.shuffle(input);

        assertThat(input).containsExactlyElementsOf(balanced);
    }

    /**
     * {@code null} and empty inputs return an empty list instead of failing.
     */
    @Test
    void nullOrEmpty_returnsEmptyList() {
        assertThat(strategy.shuffle(null)).isEmpty();
        assertThat(strategy.shuffle(List.of())).isEmpty();
    }
}
