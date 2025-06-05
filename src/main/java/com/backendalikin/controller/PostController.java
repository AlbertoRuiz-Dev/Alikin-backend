package com.backendalikin.controller;
import com.backendalikin.dto.request.PostRequest;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Post Controller", description = "Operaciones relacionadas con publicaciones")
class PostController {

    private final PostService postService;
    @Value("${app.base-url}")
    private String baseUrl;


    @Operation(summary = "Crear publicación", description = "Crea una nueva publicación del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación creada correctamente", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest, Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        return ResponseEntity.ok(postService.createGeneralPost(postRequest, userId));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear publicación con imagen", description = "Permite crear una publicación incluyendo imagen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación creada correctamente", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<PostResponse> createPostWithImage(
            @RequestPart("content") String content,
            @RequestPart(value = "songId", required = false) String songIdStr,
            @RequestPart(value = "communityId", required = false) String communityIdStr,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);

        PostRequest postRequest = new PostRequest();
        postRequest.setContent(content);

        if (songIdStr != null && !songIdStr.isBlank()) {
            postRequest.setSongId(Long.parseLong(songIdStr));
        }

        if (communityIdStr != null && !communityIdStr.isBlank()) {
            postRequest.setCommunityId(Long.parseLong(communityIdStr));
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            postRequest.setImageUrl(imageUrl);
        }

        return ResponseEntity.ok(postService.createGeneralPost(postRequest, userId));
    }


    private String saveImage(MultipartFile image) {
        try {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = Paths.get("uploads/images", filename);

            System.out.println("📥 Guardando imagen en: " + path.toAbsolutePath());

            Files.createDirectories(path.getParent());
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Imagen guardada correctamente");

            return baseUrl + "/uploads/images/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    @Operation(summary = "Obtener publicación", description = "Devuelve una publicación por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación encontrada", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id, Authentication authentication) {
        PostResponse post = postService.getPostById(id);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatus(post, userId);
        }
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "Actualizar publicación", description = "Actualiza una publicación existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación actualizada", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isPostOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest) {
        return ResponseEntity.ok(postService.updatePost(id, postRequest));
    }

    @Operation(summary = "Eliminar publicación", description = "Elimina una publicación por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicación eliminada"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isPostOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Votar publicación", description = "Permite votar una publicación (1 o -1)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voto registrado", content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido")
    })
    @PostMapping("/{id}/vote")
    public ResponseEntity<PostResponse> votePost(@PathVariable Long id, @RequestParam int value, Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        return ResponseEntity.ok(postService.votePost(id, userId, value));
    }

    @Operation(summary = "Obtener publicaciones de usuario", description = "Devuelve las publicaciones hechas por un usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(@PathVariable Long userId, @PageableDefault(size = 10) Pageable pageable, Authentication authentication) {
        Page<PostResponse> posts = postService.getUserPosts(userId, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long currentUserId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, currentUserId);
        }
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Obtener publicaciones de comunidad", description = "Devuelve publicaciones pertenecientes a una comunidad")
    @GetMapping("/community/{communityId}")
    public ResponseEntity<Page<PostResponse>> getCommunityPosts(@PathVariable Long communityId, @PageableDefault(size = 10) Pageable pageable, Authentication authentication) {
        Page<PostResponse> posts = postService.getCommunityPosts(communityId, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, userId);
        }
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Obtener feed personalizado", description = "Obtiene un feed personalizado para el usuario autenticado")
    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(@PageableDefault(size = 10) Pageable pageable, Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        Page<PostResponse> feed = postService.getFeedForUser(userId, pageable);
        postService.setUserVoteStatusForPage(feed, userId);
        return ResponseEntity.ok(feed);
    }

    @Operation(summary = "Obtener feed global de posts sin comunidad", description = "Obtiene posts de todos los usuarios que no pertenecen a ninguna comunidad.")
    @GetMapping("/feed/global")
    public ResponseEntity<Page<PostResponse>> getGlobalFeedWithoutCommunity(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            userId = postService.getUserIdByEmail(email);
        }

        Page<PostResponse> feed = postService.getGlobalPostsWithoutCommunity(pageable, userId);
        return ResponseEntity.ok(feed);
    }

}