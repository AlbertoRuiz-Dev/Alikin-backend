package com.backendalikin.controller;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.service.PlaylistService;
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
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Playlists", description = "API para gestión de listas de reproducción")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "Crear playlist")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlist creada correctamente")})
    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @RequestPart("playlistData") @Valid PlaylistRequest playlistRequest,
            @RequestPart(value = "coverImageFile", required = false) MultipartFile coverImageFile,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = playlistService.getUserIdByEmail(email);
        PlaylistResponse response = playlistService.createPlaylist(playlistRequest, userId, coverImageFile);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener playlist por ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlist obtenida correctamente")})
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @Operation(summary = "Actualizar playlist")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlist actualizada correctamente")})
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long id,
            @RequestPart("playlistData") @Valid PlaylistRequest playlistRequest,
            @RequestPart(value = "coverImageFile", required = false) MultipartFile coverImageFile,
            Authentication authentication) {
        PlaylistResponse updatedPlaylist = playlistService.updatePlaylist(id, playlistRequest, coverImageFile);
        return ResponseEntity.ok(updatedPlaylist);
    }

    @Operation(summary = "Eliminar playlist")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Playlist eliminada correctamente")})
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Añadir canción a playlist")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Canción añadida a la playlist")})
    @PostMapping("/{id}/songs/{songId}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> addSongToPlaylist(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.addSongToPlaylist(id, songId);
        return ResponseEntity.ok(new MessageResponse("Canción añadida a la playlist correctamente"));
    }

    @Operation(summary = "Eliminar canción de playlist")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Canción eliminada de la playlist")})
    @DeleteMapping("/{id}/songs/{songId}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> removeSongFromPlaylist(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.ok(new MessageResponse("Canción eliminada de la playlist correctamente"));
    }

    @Operation(summary = "Listar playlists del usuario autenticado")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlists del usuario obtenidas correctamente")})
    @GetMapping("/user")
    public ResponseEntity<List<PlaylistResponse>> getCurrentUserPlaylists(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = playlistService.getUserIdByEmail(email);
        return ResponseEntity.ok(playlistService.getPlaylistsByOwner(userId));
    }

    @Operation(summary = "Listar playlists públicas de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlists públicas obtenidas correctamente")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlaylistResponse>> getUserPlaylists(@PathVariable Long userId) {
        return ResponseEntity.ok(playlistService.getPublicPlaylistsByOwner(userId));
    }

    @Operation(summary = "Listar todas las playlists públicas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Playlists públicas listadas correctamente")})
    @GetMapping("/public")
    public ResponseEntity<List<PlaylistResponse>> getPublicPlaylists() {
        return ResponseEntity.ok(playlistService.getPublicPlaylists());
    }
}