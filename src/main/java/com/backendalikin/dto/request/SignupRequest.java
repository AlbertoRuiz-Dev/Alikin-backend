package com.backendalikin.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class SignupRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String name;

        @NotBlank(message = "El apellido es obligatorio")
        private String lastName;

        @NotBlank(message = "El nickname es obligatorio")
        @Size(min = 3, max = 20, message = "El nickname debe tener entre 3 y 20 caracteres")
        private String nickname;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        private String password;
    }

