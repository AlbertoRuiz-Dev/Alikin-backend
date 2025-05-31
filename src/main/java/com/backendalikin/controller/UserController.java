package com.backendalikin.controller;

import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

@Tag(name = "Usuarios", description = "API para la gestión de usuarios y perfiles")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Obtener perfil de usuario", description = "Devuelve el perfil de un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil recuperado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        UserResponse userResponse = userService.getUserById(id);
        // Comprobar si el usuario actual sigue a este usuario
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            userService.checkIfFollowing(email, id, userResponse);
        }
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Obtener perfil propio", description = "Devuelve el perfil del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil recuperado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del perfil de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Prohibido - No es el mismo usuario ni admin"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }


    @Operation(summary = "Subir o actualizar foto de perfil", description = "Sube o actualiza la foto de perfil de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto de perfil actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))), // Ejemplo: Map.of("profilePictureUrl", url)
            @ApiResponse(responseCode = "400", description = "Archivo inválido o demasiado grande"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<?> uploadOrUpdateProfilePicture(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "Archivo de imagen para el avatar (jpeg, png, gif)") @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication authentication) {
        String profilePictureUrl = userService.updateUserAvatar(id, avatarFile);
        return ResponseEntity.ok(java.util.Map.of("profilePictureUrl", profilePictureUrl));
    }

    @Operation(summary = "Eliminar foto de perfil", description = "Elimina la foto de perfil de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto de perfil eliminada y perfil actualizado devuelto",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}/avatar")
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeProfilePicture(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            Authentication authentication) {
        UserResponse updatedUser = userService.removeUserAvatar(id);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina una cuenta de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "Prohibido - No es el mismo usuario ni admin"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Seguir a usuario", description = "Comienza a seguir a otro usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ahora siguiendo al usuario"),
            @ApiResponse(responseCode = "400", description = "No puedes seguirte a ti mismo"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/{id}/follow")
    public ResponseEntity<MessageResponse> followUser(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long currentUserId = userService.getUserIdByEmail(email);
        userService.followUser(currentUserId, id);
        return ResponseEntity.ok(new MessageResponse("Usuario seguido correctamente"));
    }
    @Operation(summary = "Dejar de seguir", description = "Deja de seguir a un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dejado de seguir correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado o no seguido"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/{id}/unfollow")
    public ResponseEntity<MessageResponse> unfollowUser(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long currentUserId = userService.getUserIdByEmail(email);
        userService.unfollowUser(currentUserId, id);
        return ResponseEntity.ok(new MessageResponse("Ha dejado de seguir al usuario correctamente"));
    }

    @Operation(summary = "Obtener seguidores", description = "Lista los usuarios que siguen a un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserResponse>> getUserFollowers(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFollowers(id));
    }
    @Operation(summary = "Obtener seguidos", description = "Lista los usuarios que son seguidos por un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserResponse>> getUserFollowing(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFollowing(id));
    }
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre o nickname")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta"))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsersByNickname(query));
    }
}