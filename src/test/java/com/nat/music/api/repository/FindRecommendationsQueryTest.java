package com.nat.music.api.repository;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence tests for {@link SongRepository#findRecommendations}, run against H2.
 * <p>
 * Each test starts from the Liquibase seed data (8 ROCK songs) plus one JAZZ and one POP song
 * inserted here, and is rolled back afterwards, so tests do not affect each other.
 */
@DataJpaTest
class FindRecommendationsQueryTest {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Song soWhat;
    private Song billieJean;

    @BeforeEach
    void setUp() {
        soWhat = entityManager.persist(newSong("So What", "Miles Davis", Genre.JAZZ));
        billieJean = entityManager.persist(newSong("Billie Jean", "Michael Jackson", Genre.POP));
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Only songs of the requested genres are returned; songs of other genres are ignored.
     */
    @Test
    void returnsOnlySongsOfTheRequestedGenres() {
        List<Song> result = songRepository.findRecommendations(List.of(Genre.JAZZ), List.of(-1L));

        assertThat(result).extracting(Song::getTitle).containsExactly("So What");
    }

    /**
     * Several genres can be requested at once and songs of each of them are returned.
     */
    @Test
    void matchesAnyOfSeveralGenres() {
        List<Song> result = songRepository.findRecommendations(List.of(Genre.JAZZ, Genre.POP), List.of(-1L));

        assertThat(result).extracting(Song::getId).containsExactlyInAnyOrder(soWhat.getId(), billieJean.getId());
    }

    /**
     * Songs whose id is in the excluded list are not returned, even if their genre matches.
     */
    @Test
    void excludesGivenSongIds() {
        int rockSongs = songRepository.findByGenre(Genre.ROCK).size();

        List<Song> result = songRepository.findRecommendations(List.of(Genre.ROCK), List.of(1L, 2L));

        assertThat(result).hasSize(rockSongs - 2)
                .extracting(Song::getId)
                .doesNotContain(1L, 2L);
        assertThat(result).extracting(Song::getGenre).containsOnly(Genre.ROCK);
    }

    /**
     * When every song of the requested genre is excluded, an empty list is returned.
     */
    @Test
    void allMatchingSongsExcluded_returnsEmptyList() {
        List<Song> result = songRepository.findRecommendations(List.of(Genre.JAZZ), List.of(soWhat.getId()));

        assertThat(result).isEmpty();
    }

    /**
     * A genre with no song at all yields an empty list.
     */
    @Test
    void genreWithoutSongs_returnsEmptyList() {
        List<Song> result = songRepository.findRecommendations(List.of(Genre.CLASSICAL), List.of(-1L));

        assertThat(result).isEmpty();
    }

    private static Song newSong(String title, String artist, Genre genre) {
        return Song.builder().title(title).artist(artist).genre(genre).duration(300).build();
    }
}
