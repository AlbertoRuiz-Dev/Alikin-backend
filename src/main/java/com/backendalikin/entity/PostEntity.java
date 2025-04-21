package com.backendalikin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String content;
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private SongEntity song;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private CommunityEntity community;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    private int voteCount = 0;
    
    @ElementCollection
    @CollectionTable(
        name = "post_votes",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "vote_value")
    private Map<UserEntity, Integer> userVotes = new HashMap<>(); // 1 para upvote, -1 para downvote
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)  
    private List<CommentEntity> comments = new ArrayList<>();
}