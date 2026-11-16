package com.nat.music.api.service.playerservice;


import com.nat.music.api.dto.PlaylistResponseDto;

public interface PlaylistSongManagerService {

    PlaylistResponseDto addSongToPlaylist(Long playlistId, Long songId);

    PlaylistResponseDto removeSongFromPlaylist(Long playlistId, int index);

    PlaylistResponseDto reorderSongInPlaylist(Long playlistId, int fromIndex, int toIndex);
}
