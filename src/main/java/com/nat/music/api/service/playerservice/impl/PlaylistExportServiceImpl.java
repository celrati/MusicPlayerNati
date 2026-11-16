package com.nat.music.api.service.playerservice.impl;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.repository.PlaylistRepository;
import com.nat.music.api.service.playerservice.PlaylistExportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistExportServiceImpl implements PlaylistExportService {

    private final PlaylistRepository playlistRepository;

    @Override
    public Playlist getPlaylistOrThrow(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
    }
    @Override
    public String exportToM3U(Long playlistId) {
        log.info("Exporting playlist {} to M3U", playlistId);
        Playlist playlist = getPlaylistOrThrow(playlistId);

        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");
        sb.append("#PLAYLIST:").append(playlist.getName()).append("\n");

        for (Song song : playlist.getSongs()) {
            sb.append("#EXTINF:")
                    .append(song.getDuration())
                    .append(",")
                    .append(song.getArtist())
                    .append(" - ")
                    .append(song.getTitle())
                    .append("\n");

            if (song.getFilePath() == null) {
                log.warn("Song {} in playlist {} has no file path, writing 'unknown-path'", song.getId(), playlistId);
            }
            sb.append(song.getFilePath() != null ? song.getFilePath() : "unknown-path").append("\n");
        }

        log.debug("M3U export of playlist {} contains {} songs", playlistId, playlist.getSongs().size());
        return sb.toString();
    }
}
