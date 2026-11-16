package com.nat.music.api.service.playerservice;


import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;

import java.util.List;

public interface PlaylistService {

    PlaylistResponseDto createPlaylist(PlaylistRequestDto request);
    PlaylistResponseDto getPlaylistById(Long id);
    List<PlaylistResponseDto> getAllPlaylists();
    PlaylistResponseDto updatePlaylistMetadata(Long id, PlaylistRequestDto request);
    void deletePlaylist(Long id);

}
