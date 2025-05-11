package com.backendalikin.controller;

import com.backendalikin.dto.request.CommentRequest;
import com.backendalikin.dto.response.CommentResponse;
import com.backendalikin.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // 👈 esto aplica el token a todos los endpoints de la clase
@Tag(name = "Comentarios", description = "API para gestión de comentarios")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Añadir comentario", description = "Añade un comentario a una publicación existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentario añadido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = commentService.getUserIdByEmail(email);
        return ResponseEntity.ok(commentService.addComment(postId, userId, commentRequest));
    }

    @Operation(summary = "Obtener comentarios", description = "Obtiene todos los comentarios de una publicación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentarios recuperados correctamente"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @Operation(summary = "Actualizar comentario", description = "Actualiza un comentario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentario actualizado correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado a editar este comentario")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isCommentOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.updateComment(id, commentRequest));
    }

    @Operation(summary = "Eliminar comentario", description = "Elimina un comentario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentario eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado a eliminar este comentario")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isCommentOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
