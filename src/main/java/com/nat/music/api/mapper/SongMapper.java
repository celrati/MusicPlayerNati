package com.nat.music.api.mapper;


import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.dto.SongDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public class SongMapper {

    public SongDto toDto(Song song) {
        if (song == null) {
            return null;
        }
        return new SongDto(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getGenre(),
                song.getDuration(),
                song.getFilePath()
        );
    }

    public Song toEntity(SongDto dto) {
        if (dto == null) {
            return null;
        }
        return Song.builder()
                .id(dto.id())
                .title(dto.title())
                .artist(dto.artist())
                .genre(dto.genre())
                .duration(dto.duration())
                .filePath(dto.filePath())
                .build();
    }

    public List<SongDto> toDtoList(List<Song> songs) {
        if (songs == null) {
            return Collections.emptyList();
        }
        return songs.stream()
                .map(this::toDto)
                .toList();
    }
}
