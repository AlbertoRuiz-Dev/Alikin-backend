package com.backendalikin.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    private Long id;
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private boolean isPublic;
    private User owner;
    private Community community;
    private List<Song> songs = new ArrayList<>(); // Usamos List para mantener orden
}