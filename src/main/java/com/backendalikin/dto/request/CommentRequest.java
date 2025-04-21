package com.backendalikin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "El contenido es obligatorio")
    private String content;
}