package com.nat.music.api.mapper;


import com.nat.music.api.domain.entity.Playlist;
import com.nat.music.api.dto.PlaylistRequestDto;
import com.nat.music.api.dto.PlaylistResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    private final SongMapper songMapper;

    public Playlist toEntity(PlaylistRequestDto request) {
        if (request == null) {
            return null;
        }
        return Playlist.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public PlaylistResponseDto toResponseDto(Playlist playlist) {
        if (playlist == null) {
            return null;
        }

        return new PlaylistResponseDto(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt(),
                songMapper.toDtoList(playlist.getSongs()) // Délègue la conversion à SongMapper
        );
    }

    public List<PlaylistResponseDto> toResponseDtoList(List<Playlist> playlists) {
        if (playlists == null) {
            return Collections.emptyList();
        }
        return playlists.stream()
                .map(this::toResponseDto)
                .toList();
    }
}

