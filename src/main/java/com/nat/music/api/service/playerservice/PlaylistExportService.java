package com.nat.music.api.service.playerservice;


import com.nat.music.api.domain.entity.Playlist;

public interface PlaylistExportService {

    Playlist getPlaylistOrThrow(Long id);

    String exportToM3U(Long playlistId);
}
