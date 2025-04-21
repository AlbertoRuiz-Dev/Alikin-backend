package com.backendalikin.controller;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody PlaylistRequest playlistRequest,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = playlistService.getUserIdByEmail(email);
        return ResponseEntity.ok(playlistService.createPlaylist(playlistRequest, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistRequest playlistRequest) {
        return ResponseEntity.ok(playlistService.updatePlaylist(id, playlistRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/songs/{songId}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> addSongToPlaylist(
            @PathVariable Long id,
            @PathVariable Long songId) {
        playlistService.addSongToPlaylist(id, songId);
        return ResponseEntity.ok(new MessageResponse("Canción añadida a la playlist correctamente"));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    @PreAuthorize("@securityService.isPlaylistOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> removeSongFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return ResponseEntity.ok(new MessageResponse("Canción eliminada de la playlist correctamente"));
    }

    @GetMapping("/user")
    public ResponseEntity<List<PlaylistResponse>> getCurrentUserPlaylists(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = playlistService.getUserIdByEmail(email);
        return ResponseEntity.ok(playlistService.getPlaylistsByOwner(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlaylistResponse>> getUserPlaylists(@PathVariable Long userId) {
        return ResponseEntity.ok(playlistService.getPublicPlaylistsByOwner(userId));
    }

    @GetMapping("/public")
    public ResponseEntity<List<PlaylistResponse>> getPublicPlaylists() {
        return ResponseEntity.ok(playlistService.getPublicPlaylists());
    }
}