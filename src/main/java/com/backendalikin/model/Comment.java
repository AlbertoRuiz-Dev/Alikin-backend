package com.backendalikin.model;

import lombok.*;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private User user;
    private Post post;
}
