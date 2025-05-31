package com.backendalikin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String lastName;
    private String profilePictureUrl;
    private String phoneNumber;
    private String bio;
    private String nickname;
}
