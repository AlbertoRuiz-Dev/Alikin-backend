package com.backendalikin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private UserBasicResponse user;
    private SongBasicResponse song;
    private CommunityBasicResponse community;
    private LocalDateTime createdAt;
    private int voteCount;
    private int userVote; // 1, -1 o 0 según el voto del usuario actual
    private int commentsCount;
}