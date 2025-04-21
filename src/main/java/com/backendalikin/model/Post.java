package com.backendalikin.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private String content;
    private String imageUrl;
    private Song song;
    private User user;
    private Community community;
    private LocalDateTime createdAt;
    private int voteCount = 0;
    private Map<User, Integer> userVotes = new HashMap<>();
    private List<Comment> comments = new ArrayList<>();
}
