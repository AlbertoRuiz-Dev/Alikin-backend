package com.backendalikin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre debe tener entre 3 y 30 caracteres")
    private String name;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    private String imageUrl;
}
