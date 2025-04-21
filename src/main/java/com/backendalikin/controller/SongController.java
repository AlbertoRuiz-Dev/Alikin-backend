package com.backendalikin.controller;

import com.backendalikin.dto.request.SongRequest;
import com.backendalikin.dto.response.SongResponse;
import com.backendalikin.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongResponse> uploadSong(
            @RequestPart("songData") String songDataJson,
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            Authentication authentication) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        SongRequest songRequest = objectMapper.readValue(songDataJson, SongRequest.class);

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = songService.getUserIdByEmail(email);
        return ResponseEntity.ok(songService.uploadSong(songRequest, audioFile, coverImage, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongById(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamSong(@PathVariable Long id) {
        Resource audioResource = songService.getSongStreamResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(audioResource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSongUploader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<SongResponse> updateSong(
            @PathVariable Long id,
            @Valid @RequestBody SongRequest songRequest) {
        return ResponseEntity.ok(songService.updateSong(id, songRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSongUploader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SongResponse>> getUserSongs(@PathVariable Long userId) {
        return ResponseEntity.ok(songService.getSongsByUploader(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>> searchSongs(@RequestParam String query) {
        return ResponseEntity.ok(songService.searchSongs(query));
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<SongResponse>> getSongsByGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(songService.getSongsByGenre(genreId));
    }

    @GetMapping
    public ResponseEntity<Page<SongResponse>> getAllSongs(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }
}