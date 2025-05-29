package com.backendalikin.entity;

import com.backendalikin.enums.CommunityRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "communities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String name;
    
    private String description;
    private String imageUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private UserEntity leader;
    
    @ManyToMany
    @JoinTable(
        name = "community_members",
        joinColumns = @JoinColumn(name = "community_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> members = new HashSet<>();
    
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts = new ArrayList<>();
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "radio_playlist_id")
    private PlaylistEntity radioPlaylist;


    private String radioStationName;
    private String radioStreamUrl;
    private String radioStationLogoUrl;

    @ElementCollection
    @CollectionTable(
        name = "community_user_roles",
        joinColumns = @JoinColumn(name = "community_id")
    )
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Map<UserEntity, CommunityRole> userRoles = new HashMap<>();
}
