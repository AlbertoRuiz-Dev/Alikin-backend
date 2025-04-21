package com.backendalikin.repository;

import com.backendalikin.entity.CommentEntity;
import com.backendalikin.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostOrderByCreatedAtDesc(PostEntity post);
}