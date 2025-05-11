package com.backendalikin.controller;

import com.backendalikin.dto.request.SongRequest;
import com.backendalikin.dto.response.SongResponse;
import com.backendalikin.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
class SongController {

    private final SongService songService;

    @Operation(summary = "Subir canción", description = "Sube una nueva canción con archivo de audio y metadatos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Canción subida correctamente", content = @Content(schema = @Schema(implementation = SongResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongResponse> uploadSong(@RequestPart("songData") String songDataJson,
                                                   @RequestPart("audioFile") MultipartFile audioFile,
                                                   @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
                                                   Authentication authentication) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SongRequest songRequest = objectMapper.readValue(songDataJson, SongRequest.class);
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = songService.getUserIdByEmail(email);
        return ResponseEntity.ok(songService.uploadSong(songRequest, audioFile, coverImage, userId));
    }

    @Operation(summary = "Obtener canción", description = "Obtiene una canción por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongById(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @Operation(summary = "Stream de canción", description = "Transmite la canción directamente al navegador")
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamSong(@PathVariable Long id) {
        Resource audioResource = songService.getSongStreamResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(audioResource);
    }

    @Operation(summary = "Actualizar canción", description = "Actualiza los datos de una canción")
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSongUploader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<SongResponse> updateSong(@PathVariable Long id, @Valid @RequestBody SongRequest songRequest) {
        return ResponseEntity.ok(songService.updateSong(id, songRequest));
    }

    @Operation(summary = "Eliminar canción", description = "Elimina una canción por su ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSongUploader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener canciones de un usuario", description = "Lista canciones subidas por un usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SongResponse>> getUserSongs(@PathVariable Long userId) {
        return ResponseEntity.ok(songService.getSongsByUploader(userId));
    }

    @Operation(summary = "Buscar canciones", description = "Busca canciones por nombre, artista, etc")
    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>> searchSongs(@RequestParam String query) {
        return ResponseEntity.ok(songService.searchSongs(query));
    }

    @Operation(summary = "Obtener canciones por género", description = "Devuelve canciones asociadas a un género")
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<SongResponse>> getSongsByGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(songService.getSongsByGenre(genreId));
    }

    @Operation(summary = "Listar todas las canciones", description = "Devuelve un listado paginado de todas las canciones")
    @GetMapping
    public ResponseEntity<Page<SongResponse>> getAllSongs(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }
}