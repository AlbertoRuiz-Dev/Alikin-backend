package com.backendalikin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostRequest {
    @NotBlank(message = "El contenido es obligatorio")
    private String content;
    
    private String imageUrl;
    private Long songId;
    private Long communityId;
}
