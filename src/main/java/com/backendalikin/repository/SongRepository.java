package com.backendalikin.repository;

import com.backendalikin.entity.SongEntity;
import com.backendalikin.entity.GenreEntity;
import com.backendalikin.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<SongEntity, Long> {
    List<SongEntity> findByTitleContainingIgnoreCase(String title);
    List<SongEntity> findByArtistContainingIgnoreCase(String artist);
    List<SongEntity> findByUploader(UserEntity uploader);
    List<SongEntity> findByGenresContaining(GenreEntity genre);
    Page<SongEntity> findAll(Pageable pageable);
}