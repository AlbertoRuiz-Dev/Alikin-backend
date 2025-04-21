package com.backendalikin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenreRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}