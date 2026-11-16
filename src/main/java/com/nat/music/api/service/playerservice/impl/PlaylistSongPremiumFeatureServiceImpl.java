package com.nat.music.api.service.playerservice.impl;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import com.nat.music.api.dto.SongDto;
import com.nat.music.api.mapper.SongMapper;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import com.nat.music.api.service.playerservice.PlaylistSongPremiumFeatureService;
import com.nat.music.api.strategy.ShuffleStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistSongPremiumFeatureServiceImpl implements PlaylistSongPremiumFeatureService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    // inject the three implementations of the strategy of the shuffeling..
    private final Map<String, ShuffleStrategy> shuffleStrategies;

    @Override
    @Transactional(readOnly = true)
    public List<SongDto> shufflePlaylist(Long playlistId, String strategyName) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        String strategyKey = (strategyName != null) ? strategyName.toUpperCase() : "RANDOM";
        ShuffleStrategy strategy = shuffleStrategies.get(strategyKey);
        if (strategy == null) {
            log.warn("Unknown shuffle strategy '{}', falling back to RANDOM", strategyName);
            strategyKey = "RANDOM";
            strategy = shuffleStrategies.get(strategyKey);
        }
        log.info("Shuffling playlist {} ({} songs) with strategy {}", playlistId, playlist.getSongs().size(), strategyKey);
        return songMapper.toDtoList(strategy.shuffle(playlist.getSongs()));
    }


    @Override
    public List<Song> getRecommendations(Long playlistId) {

        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        List<Song> currentSongs = playlist.getSongs();

        if (currentSongs.isEmpty()) {
            log.info("Playlist {} is empty, no recommendations", playlistId);
            return List.of();
        }

        // get genres of the list of songs..
        List<Genre> playlistGenres = currentSongs.stream()
                .map(Song::getGenre)
                .distinct()
                .toList();

        // get existing songs to exclude from the search
        List<Long> excludedSongIds = currentSongs.stream()
                .map(Song::getId)
                .toList();

        List<Song> candidateSongs = songRepository.findRecommendations(playlistGenres, excludedSongIds);

        Collections.shuffle(candidateSongs);

        // return 3 recommendations..
        List<Song> recommendations = candidateSongs.stream()
                .limit(3)
                .toList();
        log.info("Recommended {} songs for playlist {} (genres {}, {} candidates)",
                recommendations.size(), playlistId, playlistGenres, candidateSongs.size());
        return recommendations;
    }
}
