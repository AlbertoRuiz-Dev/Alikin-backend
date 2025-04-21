package com.backendalikin.model;

import com.backendalikin.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String lastName;
    private String nickname;
    private String email;
    private String password;
    private String profilePicture;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean emailVerified;
    private Role role;
    private List<Post> posts = new ArrayList<>();
    private Set<User> following = new HashSet<>();
    private Set<User> followers = new HashSet<>();
    private Set<Community> communities = new HashSet<>();
    private List<Playlist> playlists = new ArrayList<>();
}