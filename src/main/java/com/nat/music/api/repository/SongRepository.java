package com.nat.music.api.repository;

import com.nat.music.api.domain.entity.Song;
import com.nat.music.api.domain.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByGenre(Genre genre);

    @Query("SELECT s FROM Song s WHERE s.genre IN :genres AND s.id NOT IN :excludedIds")
    List<Song> findRecommendations(@Param("genres") List<Genre> genres, @Param("excludedIds") List<Long> excludedIds);
}