package com.backendalikin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String lastName;
    private String nickname;
    private String email;
    private String profilePictureUrl;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean emailVerified;
    private String role;
    private int followersCount;
    private int followingCount;
    private boolean isFollowing;
    private String bio;
}