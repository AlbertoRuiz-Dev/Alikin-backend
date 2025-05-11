package com.backendalikin.controller;

import com.backendalikin.dto.request.GenreRequest;
import com.backendalikin.dto.response.GenreResponse;
import com.backendalikin.service.GenreService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Géneros", description = "API para gestión de géneros musicales")
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "Listar todos los géneros")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de géneros obtenida")})
    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @Operation(summary = "Obtener género por ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Género obtenido correctamente")})
    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @Operation(summary = "Crear nuevo género")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Género creado correctamente")})
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponse> createGenre(@Valid @RequestBody GenreRequest genreRequest) {
        return ResponseEntity.ok(genreService.createGenre(genreRequest));
    }

    @Operation(summary = "Actualizar género")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Género actualizado correctamente")})
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponse> updateGenre(@PathVariable Long id, @Valid @RequestBody GenreRequest genreRequest) {
        return ResponseEntity.ok(genreService.updateGenre(id, genreRequest));
    }

    @Operation(summary = "Eliminar género")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Género eliminado correctamente")})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}