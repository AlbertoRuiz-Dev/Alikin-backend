package com.backendalikin.controller;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.request.PostRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Importa MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Importa MultipartFile

import jakarta.validation.Valid; // Mantén esta si usas validación en los @RequestParam
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
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


    @Operation(summary = "Listar todas las comunidades", description = "Devuelve todas las comunidades existentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de comunidades recuperada correctamente",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<List<CommunityResponse>> getAllCommunities(Authentication authentication) {
        List<CommunityResponse> communities = communityService.getAllCommunities();

        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = communityService.getUserIdByEmail(email);
            communities.forEach(c -> communityService.setMembershipStatus(c, userId));
        }

        return ResponseEntity.ok(communities);
    }


    @Operation(summary = "Crear comunidad", description = "Crea una nueva comunidad musical, opcionalmente con una imagen.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comunidad creada correctamente", // Cambiado a 201 Created
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<CommunityResponse> createCommunity(
            @Parameter(description = "Nombre de la comunidad", required = true) @Valid @RequestParam("name") String name,
            @Parameter(description = "Descripción de la comunidad", required = true) @Valid @RequestParam("description") String description,
            @Parameter(description = "Archivo de imagen para la comunidad (opcional)") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);

        CommunityRequest communityRequestData = new CommunityRequest();
        communityRequestData.setName(name);
        communityRequestData.setDescription(description);

        // El método del servicio ahora debe aceptar el MultipartFile
        CommunityResponse createdCommunity = communityService.createCommunity(communityRequestData, imageFile, userId);

        return ResponseEntity.status(201).body(createdCommunity);
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
    @Operation(summary = "Actualizar comunidad", description = "Actualiza información de una comunidad, incluyendo opcionalmente una nueva imagen.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comunidad actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Prohibido - No es líder ni admin"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // Cambiar para aceptar multipart
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @Parameter(description = "ID de la comunidad a actualizar") @PathVariable Long id,
            @Parameter(description = "Nuevo nombre de la comunidad", required = true) @Valid @RequestParam("name") String name,
            @Parameter(description = "Nueva descripción de la comunidad", required = false) @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Nuevo archivo de imagen para la comunidad (opcional)") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) { // La autenticación ya está siendo usada por @PreAuthorize

        // Crear un CommunityRequest para pasar al servicio
        CommunityRequest communityRequestData = new CommunityRequest();
        communityRequestData.setName(name);
        if (description != null) {
            communityRequestData.setDescription(description);
        }
        CommunityResponse updatedCommunity = communityService.updateCommunityWithImage(id, communityRequestData, imageFile);
        return ResponseEntity.ok(updatedCommunity);
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
                    content = @Content(schema = @Schema(implementation = UserResponse.class))), // Ajustado para ser más específico
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
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))), // Ajustado
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
                    content = @Content(schema = @Schema(implementation = CommunityResponse.class))), // Ajustado
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/user")
    public ResponseEntity<List<CommunityResponse>> getUserCommunities(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        List<CommunityResponse> communities = communityService.getUserCommunities(userId);
        communities.forEach(community -> community.setMember(true)); // Esto ya se hace en el servicio, pero no hace daño
        return ResponseEntity.ok(communities);
    }

    @Operation(summary = "Crear un nuevo post en una comunidad", description = "El usuario autenticado crea un post en la comunidad especificada. Puede incluir texto, una imagen y/o una canción.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post creado correctamente",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido (ej. no es miembro)"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada")
    })
    @PostMapping(value = "/{communityId}/posts", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // <-- CLAVE: consumes multipart
    public ResponseEntity<PostResponse> createCommunityPost(
            @Parameter(description = "ID de la comunidad donde se creará el post") @PathVariable Long communityId,
            @Parameter(description = "Contenido de texto del post (opcional si hay adjuntos)", required = false) @RequestParam(value = "content", required = false, defaultValue = "") String content,
            @Parameter(description = "ID de una canción existente a adjuntar (opcional)") @RequestParam(value = "songId", required = false) Long songId,
            @Parameter(description = "Archivo de imagen a adjuntar (opcional)") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @Parameter(description = "Archivo de música a adjuntar (opcional)") @RequestPart(value = "songFile", required = false) MultipartFile songFile,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Long userId = postService.getUserIdByEmail(email); // O communityService.getUserIdByEmail(email)

        // Necesitas un DTO para pasar al servicio, o modificar el servicio para tomar estos parámetros.
        // Crearemos un PostRequest aquí.
        PostRequest postRequestData = new PostRequest();
        postRequestData.setContent(content);
        if (songId != null) {
            postRequestData.setSongId(songId);
        }

        PostResponse newPost = postService.createPostInCommunityAndHandleFiles(
                communityId,
                postRequestData, // DTO con content y songId
                imageFile,       // Archivo de imagen opcional
                songFile,        // Archivo de canción opcional
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    @PutMapping("/{id}/radio-station")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')") // Solo el líder o admin
    public ResponseEntity<CommunityResponse> setCommunityRadioStation(
            @PathVariable Long id,
            @RequestParam String stationName,
            @RequestParam String streamUrl,
            @RequestParam(required = false) String stationLogoUrl,
            Authentication authentication) {

        CommunityResponse updatedCommunity = communityService.setCommunityRadioStation(id, stationName, streamUrl, stationLogoUrl);
        return ResponseEntity.ok(updatedCommunity);
    }



    @Operation(summary = "Expulsar miembro de la comunidad",
            description = "El líder de la comunidad expulsa a un miembro. El líder no puede expulsarse a sí mismo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miembro expulsado correctamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. intentar expulsar al líder, o el usuario no es miembro)"),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario autenticado no es el líder de esta comunidad"),
            @ApiResponse(responseCode = "404", description = "Comunidad o miembro no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{communityId}/members/{memberId}")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #communityId)")
    public ResponseEntity<MessageResponse> kickMember(
            @Parameter(description = "ID de la comunidad") @PathVariable Long communityId,
            @Parameter(description = "ID del miembro a expulsar") @PathVariable Long memberId,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String leaderEmail = userDetails.getUsername();
        // El servicio necesitará el ID del líder para validar que no se está auto-expulsando.
        Long leaderId = communityService.getUserIdByEmail(leaderEmail);


        communityService.kickMember(communityId, memberId, leaderId);
        return ResponseEntity.ok(new MessageResponse("Miembro expulsado correctamente de la comunidad."));
    }
}