package com.nat.music.api.strategy;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.strategy.impl.SmartShuffleStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nat.music.api.strategy.ShuffleTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SmartShuffleStrategy}, which avoids playing the same artist twice in a row.
 * <p>
 * Randomized properties are checked over {@code RUNS} shuffles.
 */
class SmartShuffleStrategyTest {

    private final SmartShuffleStrategy strategy = new SmartShuffleStrategy();

    /** 3 songs by Queen + 3 by Muse: a perfect alternation always exists. */
    private final List<Song> twoArtists = List.of(
            song("Bohemian Rhapsody", "Queen", Genre.ROCK),
            song("Somebody to Love", "Queen", Genre.ROCK),
            song("Under Pressure", "Queen", Genre.ROCK),
            song("Uprising", "Muse", Genre.ROCK),
            song("Hysteria", "Muse", Genre.ROCK),
            song("Starlight", "Muse", Genre.ROCK));

    /**
     * Every shuffle returns the same songs as the input: none lost, duplicated or added.
     */
    @Test
    void returnsExactlyTheSameSongs() {
        for (int i = 0; i < RUNS; i++) {
            assertThat(strategy.shuffle(twoArtists)).containsExactlyInAnyOrderElementsOf(twoArtists);
        }
    }

    /**
     * With two artists having the same number of songs, a perfect alternation exists, so no
     * two consecutive songs may share an artist.
     */
    @Test
    void neverPlaysTheSameArtistTwiceInARow_whenTwoArtistsCanAlternate() {
        for (int i = 0; i < RUNS; i++) {
            List<Song> result = strategy.shuffle(twoArtists);
            assertThat(adjacentDuplicates(result, Song::getArtist))
                    .as("same artist back to back in %s", result.stream().map(Song::getTitle).toList())
                    .isZero();
        }
    }

    /**
     * Artist names that differ only by case ({@code Queen}/{@code QUEEN}) are treated as the same artist.
     */
    @Test
    void comparesArtistsIgnoringCase() {
        List<Song> mixedCase = List.of(
                song("Bohemian Rhapsody", "Queen", Genre.ROCK),
                song("Somebody to Love", "QUEEN", Genre.ROCK),
                song("Uprising", "Muse", Genre.ROCK),
                song("Hysteria", "muse", Genre.ROCK));

        for (int i = 0; i < RUNS; i++) {
            List<Song> result = strategy.shuffle(mixedCase);
            assertThat(adjacentDuplicates(result, s -> s.getArtist().toLowerCase())).isZero();
        }
    }

    /**
     * Avoiding same-artist neighbours must not make the output deterministic.
     */
    @Test
    void stillShuffles() {
        Set<List<Song>> orders = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            orders.add(strategy.shuffle(twoArtists));
        }
        assertThat(orders).hasSizeGreaterThan(10);
    }

    /**
     * When every song has the same artist, back-to-back songs are unavoidable and all songs are
     * still returned.
     */
    @Test
    void singleArtist_returnsAllSongs() {
        List<Song> oneArtist = twoArtists.subList(0, 3);

        assertThat(strategy.shuffle(oneArtist)).containsExactlyInAnyOrderElementsOf(oneArtist);
    }

    /**
     * The input list is left in its original order; the strategy works on a copy.
     */
    @Test
    void doesNotModifyTheInputList() {
        List<Song> input = new ArrayList<>(twoArtists);

        strategy.shuffle(input);

        assertThat(input).containsExactlyElementsOf(twoArtists);
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
