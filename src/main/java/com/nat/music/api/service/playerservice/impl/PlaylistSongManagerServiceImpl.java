package com.nat.music.api.service.playerservice.impl;

import com.nat.music.api.dto.PlaylistResponseDto;
import com.nat.music.api.mapper.PlaylistMapper;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.repository.SongRepository;
import com.nat.music.api.service.playerservice.PlaylistSongManagerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistSongManagerServiceImpl implements PlaylistSongManagerService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;

    @Override
    public PlaylistResponseDto addSongToPlaylist(Long playlistId, Long songId) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        var song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + songId));

        playlist.getSongs().add(song);
        playlist.touch();

        var newPlaylist = playlistRepository.save(playlist);
        log.info("Added song {} to playlist {} (now {} songs)", songId, playlistId, newPlaylist.getSongs().size());
        return playlistMapper.toResponseDto(newPlaylist);
    }

    @Override
    public PlaylistResponseDto removeSongFromPlaylist(Long playlistId, int index) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        var playListSize = playlist.getSongs().size();

        if (index < 0 || index >= playListSize) {
            throw new IllegalArgumentException("Invalid song index: " + index);
        }

        var removed = playlist.getSongs().remove(index);
        playlist.touch();
        var newPlaylist = playlistRepository.save(playlist);
        log.info("Removed song {} at index {} from playlist {}", removed.getId(), index, playlistId);
        return playlistMapper.toResponseDto(newPlaylist);
    }

    @Override
    public PlaylistResponseDto reorderSongInPlaylist(Long playlistId, int fromIndex, int toIndex) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));

        int playListSize = playlist.getSongs().size();

        if (fromIndex < 0 || fromIndex >= playListSize) {
            throw new IllegalArgumentException("Invalid song index: " + fromIndex);
        }
        if (toIndex < 0 || toIndex >= playListSize) {
            throw new IllegalArgumentException("Invalid song index: " + toIndex);
        }


        var songToMove = playlist.getSongs().remove(fromIndex);
        playlist.getSongs().add(toIndex, songToMove);
        playlist.touch();

        var newPlaylist = playlistRepository.save(playlist);
        log.info("Moved song {} in playlist {} from index {} to {}", songToMove.getId(), playlistId, fromIndex, toIndex);

        return playlistMapper.toResponseDto(newPlaylist);
    }

}
