package com.backendalikin.repository;

import com.backendalikin.entity.PostEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.entity.CommunityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findByUser(UserEntity user, Pageable pageable);
    Page<PostEntity> findByCommunity(CommunityEntity community, Pageable pageable);
    Page<PostEntity> findByUserIn(List<UserEntity> users, Pageable pageable);
    Page<PostEntity> findByOrderByVoteCountDesc(Pageable pageable);
    Page<PostEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<PostEntity> findByCommunityIdOrderByCreatedAtDesc(Long communityId, Pageable pageable);
    @Query("SELECT p FROM PostEntity p WHERE p.community IS NULL ORDER BY p.createdAt DESC")
    Page<PostEntity> findGlobalPostsWithoutCommunity(Pageable pageable);
    Page<PostEntity> findByUserInOrCommunityInOrderByCreatedAtDesc(List<UserEntity> users, List<CommunityEntity> communities, Pageable pageable);
}