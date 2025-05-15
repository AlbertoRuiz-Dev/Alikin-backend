package com.backendalikin.service;

import com.backendalikin.dto.request.SongRequest;
import com.backendalikin.dto.response.SongResponse;
import com.backendalikin.entity.GenreEntity;
import com.backendalikin.entity.SongEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.SongMapper;
import com.backendalikin.repository.GenreRepository;
import com.backendalikin.repository.SongRepository;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public SongResponse uploadSong(SongRequest songRequest, MultipartFile audioFile,
                                   MultipartFile coverImage, Long userId) throws IOException {
        UserEntity uploader = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar archivos
        if (audioFile.isEmpty()) {
            throw new RuntimeException("El archivo de audio no puede estar vacío");
        }

        // Crear nueva entidad de canción
        SongEntity song = songMapper.toEntity(songRequest);
        song.setUploader(uploader);
        song.setUploadedAt(LocalDateTime.now());

        // Guardar archivo de audio
        String audioUrl = fileStorageService.storeFile(audioFile, "songs");
        song.setUrl(audioUrl);

        // Guardar imagen de portada si existe
        if (coverImage != null && !coverImage.isEmpty()) {
            String coverImageUrl = fileStorageService.storeFile(coverImage, "covers");
            song.setCoverImageUrl(coverImageUrl);
        } else {
            song.setCoverImageUrl(null);
        }

        // Calcular duración (esto es un ejemplo, en un caso real necesitarías una biblioteca para obtener la duración)
        song.setDuration(180); // 3 minutos como ejemplo

        // Procesar géneros
        if (songRequest.getGenres() != null && !songRequest.getGenres().isEmpty()) {
            Set<GenreEntity> genres = new HashSet<>();
            for (String genreName : songRequest.getGenres()) {
                GenreEntity genre = genreRepository.findByNameIgnoreCase(genreName)
                        .orElseGet(() -> {
                            GenreEntity newGenre = new GenreEntity();
                            newGenre.setName(genreName);
                            return genreRepository.save(newGenre);
                        });
                genres.add(genre);
            }
            song.setGenres(genres);
        }

        SongEntity savedSong = songRepository.save(song);
        return songMapper.toSongResponse(savedSong);
    }

    @Transactional(readOnly = true)
    public SongResponse getSongById(Long id) {
        SongEntity song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));
        return songMapper.toSongResponse(song);
    }

    @Transactional(readOnly = true)
    public Resource getSongStreamResource(Long id) {
        SongEntity song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        byte[] audioData = fileStorageService.loadFileAsBytes(song.getUrl());
        return new ByteArrayResource(audioData);
    }

    @Transactional
    public SongResponse updateSong(Long id, SongRequest songRequest) {
        SongEntity song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        songMapper.updateSongFromRequest(songRequest, song);

        // Actualizar géneros si se proporcionan
        if (songRequest.getGenres() != null && !songRequest.getGenres().isEmpty()) {
            Set<GenreEntity> genres = new HashSet<>();
            for (String genreName : songRequest.getGenres()) {
                GenreEntity genre = genreRepository.findByNameIgnoreCase(genreName)
                        .orElseGet(() -> {
                            GenreEntity newGenre = new GenreEntity();
                            newGenre.setName(genreName);
                            return genreRepository.save(newGenre);
                        });
                genres.add(genre);
            }
            song.setGenres(genres);
        }

        SongEntity updatedSong = songRepository.save(song);
        return songMapper.toSongResponse(updatedSong);
    }

    @Transactional
    public void deleteSong(Long id) {
        SongEntity song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        // Eliminar archivos asociados
        if (song.getUrl() != null) {
            fileStorageService.deleteFile(song.getUrl());
        }
        if (song.getCoverImageUrl() != null && !song.getCoverImageUrl().equals("default-cover.jpg")) {
            fileStorageService.deleteFile(song.getCoverImageUrl());
        }

        songRepository.delete(song);
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByUploader(Long uploaderId) {
        UserEntity uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<SongEntity> songs = songRepository.findByUploader(uploader);
        return songs.stream()
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongResponse> searchSongs(String query) {
        // Buscar por título o artista
        List<SongEntity> titleMatches = songRepository.findByTitleContainingIgnoreCase(query);
        List<SongEntity> artistMatches = songRepository.findByArtistContainingIgnoreCase(query);

        // Combinar resultados sin duplicados
        Set<SongEntity> combinedResults = new HashSet<>(titleMatches);
        combinedResults.addAll(artistMatches);

        return combinedResults.stream()
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByGenre(Long genreId) {
        GenreEntity genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Género no encontrado"));

        List<SongEntity> songs = songRepository.findByGenresContaining(genre);
        return songs.stream()
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SongResponse> getAllSongs(Pageable pageable) {
        Page<SongEntity> songs = songRepository.findAll(pageable);
        return songs.map(songMapper::toSongResponse);
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email))
                .getId();
    }
}