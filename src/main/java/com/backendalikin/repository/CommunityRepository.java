package com.backendalikin.repository;

import com.backendalikin.entity.CommunityEntity;
import com.backendalikin.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityEntity, Long> {
    Optional<CommunityEntity> findByName(String name);
    boolean existsByName(String name);
    List<CommunityEntity> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM CommunityEntity c WHERE :user MEMBER OF c.members")
    List<CommunityEntity> findCommunitiesByMember(UserEntity user);
    
    List<CommunityEntity> findByLeader(UserEntity leader);
}