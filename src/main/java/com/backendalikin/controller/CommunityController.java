package com.backendalikin.controller;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.service.CommunityService;
import com.backendalikin.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comunidades", description = "API para la gestión de comunidades musicales")
public class CommunityController {

    private final CommunityService communityService;
    private final PostService postService;

    @Operation(summary = "Crear comunidad", description = "Crea una nueva comunidad musical")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comunidad creada correctamente",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityRequest communityRequest,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        return ResponseEntity.ok(communityService.createCommunity(communityRequest, userId));
    }

    @Operation(summary = "Obtener comunidad", description = "Obtiene información detallada de una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comunidad recuperada correctamente",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(
            @PathVariable Long id,
            Authentication authentication) {
        CommunityResponse community = communityService.getCommunityById(id);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = communityService.getUserIdByEmail(email);
            communityService.setMembershipStatus(community, userId);
        }
        return ResponseEntity.ok(community);
    }

    @Operation(summary = "Actualizar comunidad", description = "Actualiza información de una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comunidad actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Prohibido - No es líder ni admin"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable Long id,
            @Valid @RequestBody CommunityRequest communityRequest) {
        return ResponseEntity.ok(communityService.updateCommunity(id, communityRequest));
    }

    @Operation(summary = "Eliminar comunidad", description = "Elimina una comunidad existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comunidad eliminada correctamente"),
            @ApiResponse(responseCode = "403", description = "Prohibido - No es líder ni admin"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unirse a comunidad", description = "El usuario autenticado se une a una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unido a la comunidad correctamente"),
            @ApiResponse(responseCode = "400", description = "Ya eres miembro de esta comunidad"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/{id}/join")
    public ResponseEntity<MessageResponse> joinCommunity(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        communityService.joinCommunity(id, userId);
        return ResponseEntity.ok(new MessageResponse("Te has unido a la comunidad correctamente"));
    }

    @Operation(summary = "Abandonar comunidad", description = "El usuario autenticado abandona una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comunidad abandonada correctamente"),
            @ApiResponse(responseCode = "400", description = "No eres miembro de esta comunidad"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/{id}/leave")
    public ResponseEntity<MessageResponse> leaveCommunity(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        communityService.leaveCommunity(id, userId);
        return ResponseEntity.ok(new MessageResponse("Has abandonado la comunidad correctamente"));
    }

    @Operation(summary = "Listar miembros", description = "Obtiene la lista de miembros de una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta con token JWT"))),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<List<UserResponse>> getCommunityMembers(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getCommunityMembers(id));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<Page<PostResponse>> getCommunityPosts(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        Page<PostResponse> posts = postService.getCommunityPosts(id, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, userId);
        }
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{id}/radio")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> setCommunityRadio(
            @PathVariable Long id,
            @RequestParam Long playlistId) {
        communityService.setCommunityRadio(id, playlistId);
        return ResponseEntity.ok(new MessageResponse("Radio de comunidad actualizada correctamente"));
    }

    @Operation(summary = "Buscar comunidades", description = "Busca comunidades por nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta con token JWT"))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/search")
    public ResponseEntity<List<CommunityResponse>> searchCommunities(
            @RequestParam String query,
            Authentication authentication) {
        List<CommunityResponse> communities = communityService.searchCommunities(query);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = communityService.getUserIdByEmail(email);
            communities.forEach(community -> communityService.setMembershipStatus(community, userId));
        }
        return ResponseEntity.ok(communities);
    }
    @Operation(summary = "Listar comunidades de usuario", description = "Obtiene las comunidades a las que pertenece un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada correctamente",
                    content = @Content(schema = @Schema(description = "Respuesta con token JWT"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/user")
    public ResponseEntity<List<CommunityResponse>> getUserCommunities(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        List<CommunityResponse> communities = communityService.getUserCommunities(userId);
        communities.forEach(community -> community.setMember(true));
        return ResponseEntity.ok(communities);
    }
}