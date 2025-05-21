package com.backendalikin.model;

import com.backendalikin.enums.CommunityRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Community {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private User leader;
    private Set<User> members = new HashSet<>();
    private List<Post> posts = new ArrayList<>();
    private Playlist radioPlaylist;
    private Map<User, CommunityRole> userRoles = new HashMap<>();
}
