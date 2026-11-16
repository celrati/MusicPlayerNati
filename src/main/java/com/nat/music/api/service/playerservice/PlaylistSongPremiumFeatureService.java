package com.nat.music.api.service.playerservice;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.dto.SongDto;

import java.util.List;

public interface PlaylistSongPremiumFeatureService {

    List<SongDto> shufflePlaylist(Long playlistId, String strategyName);

    public List<Song> getRecommendations(Long playlistId);
}
