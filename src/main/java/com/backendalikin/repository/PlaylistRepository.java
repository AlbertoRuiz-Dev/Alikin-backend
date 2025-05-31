package com.backendalikin.repository;

import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
    List<PlaylistEntity> findByOwner(UserEntity owner);
    List<PlaylistEntity> findByOwnerAndIsPublicTrue(UserEntity owner);
    List<PlaylistEntity> findByIsPublicTrue();
    @Query("SELECT p FROM PlaylistEntity p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.songs WHERE p.id = :id")
    Optional<PlaylistEntity> findByIdWithOwnerAndSongs(@Param("id") Long id);

}