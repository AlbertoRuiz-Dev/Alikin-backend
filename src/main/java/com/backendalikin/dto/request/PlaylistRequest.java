package com.backendalikin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    private String description;
    private String coverImageUrl;
    private boolean isPublic;
    private List<Long> songIds;
}