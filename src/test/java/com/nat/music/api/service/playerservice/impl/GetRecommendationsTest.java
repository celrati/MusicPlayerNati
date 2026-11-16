package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;

import java.util.*;

import static com.nat.music.api.fixtures.EntityFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlaylistSongPremiumFeatureServiceImpl#getRecommendations}.
 * <p>
 * The repositories are mocked, so these tests check how the service builds the query arguments
 * (genres to include, songs to exclude) and what it does with the candidates. The query itself is
 * covered by {@code FindRecommendationsQueryTest}.
 */
@ExtendWith(MockitoExtension.class)
class GetRecommendationsTest {

    private static final int RUNS = 200;

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private SongRepository songRepository;

    private PlaylistSongPremiumFeatureServiceImpl service;

    private final Song bohemian = song(1L, "Bohemian Rhapsody", "Queen", Genre.ROCK);
    private final Song stairway = song(2L, "Stairway to Heaven", "Led Zeppelin", Genre.ROCK);
    private final Song soWhat = song(10L, "So What", "Miles Davis", Genre.JAZZ);

    @BeforeEach
    void setUp() {
        service = newPremiumFeatureService(playlistRepository, songRepository, Map.of());
    }

    /**
     * The query receives each genre of the playlist once, in first-seen order, and the ids of every
     * song already in the playlist so they are not recommended again.
     */
    @Test
    void queriesDistinctGenresAndExcludesPlaylistSongs() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian, soWhat, stairway)));
        when(songRepository.findRecommendations(anyList(), anyList())).thenReturn(new ArrayList<>());

        service.getRecommendations(1L);

        verify(songRepository).findRecommendations(List.of(Genre.ROCK, Genre.JAZZ), List.of(1L, 10L, 2L));
    }

    /**
     * When more than three candidates match, exactly three are returned, all taken from the candidates.
     */
    @Test
    void returnsAtMostThreeCandidates() {
        List<Song> candidates = candidates(5);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian)));
        when(songRepository.findRecommendations(anyList(), anyList())).thenReturn(new ArrayList<>(candidates));

        List<Song> result = service.getRecommendations(1L);

        assertThat(result).hasSize(3).doesNotHaveDuplicates();
        assertThat(candidates).containsAll(result);
    }

    /**
     * When three or fewer candidates match, all of them are returned.
     */
    @Test
    void fewerThanThreeCandidates_returnsAllOfThem() {
        List<Song> candidates = candidates(2);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian)));
        when(songRepository.findRecommendations(anyList(), anyList())).thenReturn(new ArrayList<>(candidates));

        assertThat(service.getRecommendations(1L)).containsExactlyInAnyOrderElementsOf(candidates);
    }

    /**
     * Candidates are shuffled before the top three are taken, so repeated calls do not always
     * recommend the same songs.
     */
    @Test
    void picksRandomCandidates() {
        List<Song> candidates = candidates(6);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian)));
        when(songRepository.findRecommendations(anyList(), anyList()))
                .thenAnswer(invocation -> new ArrayList<>(candidates));

        Set<Set<Song>> picks = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            picks.add(Set.copyOf(service.getRecommendations(1L)));
        }

        assertThat(picks).hasSizeGreaterThan(1);
    }

    /**
     * When no song matches, an empty list is returned rather than an error.
     */
    @Test
    void noCandidates_returnsEmptyList() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L, bohemian)));
        when(songRepository.findRecommendations(anyList(), anyList())).thenReturn(new ArrayList<>());

        assertThat(service.getRecommendations(1L)).isEmpty();
    }

    /**
     * An empty playlist has no genre to base recommendations on: an empty list is returned and the
     * song repository is not queried.
     */
    @Test
    void emptyPlaylist_returnsEmptyListWithoutQuerying() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist(1L)));

        assertThat(service.getRecommendations(1L)).isEmpty();
        verifyNoInteractions(songRepository);
    }

    /**
     * Recommending is read-only: the playlist's songs and {@code updatedAt} are unchanged and
     * nothing is saved.
     */
    @Test
    void doesNotChangeThePlaylist() {
        var existing = playlist(1L, bohemian, stairway);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(songRepository.findRecommendations(anyList(), anyList())).thenReturn(new ArrayList<>(candidates(4)));

        service.getRecommendations(1L);

        assertThat(existing.getSongs()).containsExactly(bohemian, stairway);
        assertThat(existing.getUpdatedAt()).isEqualTo(OLD_TIMESTAMP);
        verify(playlistRepository, never()).save(any());
    }

    /**
     * A missing playlist raises {@code EntityNotFoundException} before any song lookup.
     */
    @Test
    void unknownPlaylist_throwsAndDoesNotQuerySongs() {
        when(playlistRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecommendations(9999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Playlist not found with id: 9999");
        verifyNoInteractions(songRepository);
    }

    /** {@code count} ROCK songs that are not in any test playlist. */
    private static List<Song> candidates(int count) {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            songs.add(song(100L + i, "Candidate " + i, "Artist " + i, Genre.ROCK));
        }
        return songs;
    }
}
