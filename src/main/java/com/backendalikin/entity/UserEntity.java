package com.backendalikin.entity;

import com.backendalikin.model.enums.Role;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String lastName;
    
    @Column(unique = true)
    private String nickname;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    private String profilePictureUrl;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean emailVerified;
    private String bio;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "user_follows",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "followed_id")
    )
    private Set<UserEntity> following = new HashSet<>();


    @ManyToMany(mappedBy = "following")
    private Set<UserEntity> followers = new HashSet<>();
    
    @ManyToMany(mappedBy = "members")
    private Set<CommunityEntity> communities = new HashSet<>();
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaylistEntity> playlists = new ArrayList<>();
}