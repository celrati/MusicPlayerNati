package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.mapper.PlaylistMapper;
import com.nat.music.api.mapper.SongMapper;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import com.nat.music.api.service.playerservice.PlaylistService;
import com.nat.music.api.strategy.ShuffleStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;
    private final SongMapper songMapper;

    // inject the three implementations of the strategy of the shuffeling..
    private final Map<String, ShuffleStrategy> shuffleStrategies;

    @Override
    public PlaylistResponseDto createPlaylist(PlaylistRequestDto request) {
        Playlist playlist = Playlist.builder()
                .name(request.name())
                .description(request.description())
                .build();
        Playlist saved = playlistRepository.save(playlist);
        log.info("Created playlist {} with name '{}'", saved.getId(), saved.getName());
        return playlistMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistResponseDto getPlaylistById(Long id) {
        log.debug("Fetching playlist {}", id);
        var playList = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        return playlistMapper.toResponseDto(playList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getAllPlaylists() {
        List<PlaylistResponseDto> playlists = playlistRepository.findAll().stream()
                .map(playlistMapper::toResponseDto)
                .toList();
        log.debug("Fetched {} playlists", playlists.size());
        return playlists;
    }

    @Override
    public PlaylistResponseDto updatePlaylistMetadata(Long id, PlaylistRequestDto request) {
        var playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        playlist.touch();
        Playlist saved = playlistRepository.save(playlist);
        log.info("Updated metadata of playlist {} (name '{}')", id, saved.getName());
        return playlistMapper.toResponseDto(saved);
    }

    @Override
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new EntityNotFoundException("Playlist not found with id: " + id);
        }
        playlistRepository.deleteById(id);
        log.info("Deleted playlist {}", id);
    }

}
