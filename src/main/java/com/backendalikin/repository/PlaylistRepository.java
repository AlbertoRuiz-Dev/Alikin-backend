package com.backendalikin.repository;

import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
    List<PlaylistEntity> findByOwner(UserEntity owner);
    List<PlaylistEntity> findByOwnerAndIsPublicTrue(UserEntity owner);
    List<PlaylistEntity> findByIsPublicTrue();
}