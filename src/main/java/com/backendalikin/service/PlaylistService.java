package com.backendalikin.service;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.SongEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.exception.FileUploadException;

import com.backendalikin.service.ResourceNotFoundException;
import com.backendalikin.mapper.PlaylistMapper;
import com.backendalikin.repository.PlaylistRepository;
import com.backendalikin.repository.SongRepository;
import com.backendalikin.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;
    private final Path fileStorageLocationAbs;
    private final String publicUrlPathSegment;

    public PlaylistService(PlaylistRepository playlistRepository,
                           UserRepository userRepository,
                           SongRepository songRepository,
                           PlaylistMapper playlistMapper,
                           @Value("${app.upload.dir.playlist-covers}") String uploadDirAbsPath,
                           @Value("${app.file.storage.base-url-segment.playlist-covers}") String publicUrlSegment) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.playlistMapper = playlistMapper;
        this.publicUrlPathSegment = publicUrlSegment;
        this.fileStorageLocationAbs = Paths.get(uploadDirAbsPath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocationAbs);
        } catch (Exception ex) {
            throw new FileUploadException("No se pudo crear el directorio para almacenar las portadas de playlist: " + this.fileStorageLocationAbs.toString(), ex);
        }
    }

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest playlistRequest, Long userId, MultipartFile coverImageFile) { //
        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        PlaylistEntity playlist = playlistMapper.toEntity(playlistRequest); //
        playlist.setOwner(owner);
        playlist.setCreatedAt(LocalDateTime.now()); //

        if (coverImageFile != null && !coverImageFile.isEmpty()) { //
            String fileName = storeFile(coverImageFile); //
            String coverImageUrl = Paths.get(this.publicUrlPathSegment, fileName).toString().replace("\\", "/"); //
            playlist.setCoverImageUrl(coverImageUrl);
        } else if (playlistRequest.getCoverImageUrl() != null && !playlistRequest.getCoverImageUrl().trim().isEmpty()){ //
            playlist.setCoverImageUrl(playlistRequest.getCoverImageUrl());
        }

        playlist.setSongs(new ArrayList<>()); //
        if (playlistRequest.getSongIds() != null && !playlistRequest.getSongIds().isEmpty()) { //
            List<SongEntity> songs = songRepository.findAllById(playlistRequest.getSongIds()); //
            playlist.setSongs(new ArrayList<>(songs)); //
        }

        PlaylistEntity savedPlaylist = playlistRepository.save(playlist); //
        PlaylistEntity entityForResponse = playlistRepository.findByIdWithOwnerAndSongs(savedPlaylist.getId()) //
                .orElse(savedPlaylist); //

        // --- LOGS DE DEPURACIÓN ---
        System.out.println("PASO 6 (Service createPlaylist): Valor de 'isPublic' en 'entityForResponse' (desde BD) ANTES de mapear a DTO: " + entityForResponse.isPublic()); //

        PlaylistResponse responseDto =  playlistMapper.toPlaylistResponse(entityForResponse); //

        // Asumiendo que PlaylistResponse tiene un getter isPublic() porque el campo es 'boolean isPublic'
        System.out.println("PASO 7 (Service createPlaylist): Valor de 'isPublic' en 'responseDto' (DTO Java mapeado) ANTES de retornar: " + responseDto.isPublic()); //
        return responseDto;
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long id) { //
        PlaylistEntity playlist = playlistRepository.findByIdWithOwnerAndSongs(id) //
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + id));

        // --- AÑADIR LOGS AQUÍ TAMBIÉN SI ESTE MÉTODO SE USA DESPUÉS DE EDITAR ---
        System.out.println("PASO 6 (Service getPlaylistById): Valor de 'isPublic' en 'playlist' (desde BD) ANTES de mapear a DTO: " + playlist.isPublic());

        PlaylistResponse responseDto = playlistMapper.toPlaylistResponse(playlist); //

        System.out.println("PASO 7 (Service getPlaylistById): Valor de 'isPublic' en 'responseDto' (DTO Java mapeado) ANTES de retornar: " + responseDto.isPublic());
        return responseDto;
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId, PlaylistRequest playlistRequest, MultipartFile coverImageFile) { //
        PlaylistEntity playlist = playlistRepository.findByIdWithOwnerAndSongs(playlistId) //
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + playlistId));

        playlistMapper.updatePlaylistFromRequest(playlistRequest, playlist); //

        if (coverImageFile != null && !coverImageFile.isEmpty()) { //
            String fileName = storeFile(coverImageFile); //
            String coverImageUrl = Paths.get(this.publicUrlPathSegment, fileName).toString().replace("\\", "/"); //
            playlist.setCoverImageUrl(coverImageUrl);
        } else if (playlistRequest.getCoverImageUrl() != null && playlistRequest.getCoverImageUrl().isEmpty()){ //
            playlist.setCoverImageUrl(null); //
        }

        if (playlistRequest.getSongIds() != null) { //
            playlist.getSongs().clear(); //
            List<SongEntity> songs = songRepository.findAllById(playlistRequest.getSongIds()); //
            playlist.setSongs(new ArrayList<>(songs)); //
        }

        PlaylistEntity updatedPlaylist = playlistRepository.save(playlist); //
        PlaylistEntity entityForResponse = playlistRepository.findByIdWithOwnerAndSongs(updatedPlaylist.getId()) //
                .orElse(updatedPlaylist); //

        // --- AÑADIR LOGS AQUÍ TAMBIÉN ---
        System.out.println("PASO 6 (Service updatePlaylist): Valor de 'isPublic' en 'entityForResponse' (desde BD) ANTES de mapear a DTO: " + entityForResponse.isPublic());

        PlaylistResponse responseDto = playlistMapper.toPlaylistResponse(entityForResponse); //

        System.out.println("PASO 7 (Service updatePlaylist): Valor de 'isPublic' en 'responseDto' (DTO Java mapeado) ANTES de retornar: " + responseDto.isPublic());
        return responseDto;
    }

    private String storeFile(MultipartFile file) { //
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename()); //
        String fileExtension = ""; //
        try { //
            if (originalFileName == null || originalFileName.lastIndexOf(".") < 0) { //
                throw new FileUploadException("El archivo no tiene una extensión válida."); //
            }
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); //
            if (!List.of(".png", ".jpg", ".jpeg", ".gif").contains(fileExtension.toLowerCase())) { //
                throw new FileUploadException("Extensión de archivo no permitida: " + fileExtension); //
            }
        } catch (StringIndexOutOfBoundsException e) { //
            throw new FileUploadException("Nombre de archivo original inválido: " + originalFileName, e); //
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension; //

        try { //
            if (file.isEmpty()) { //
                throw new FileUploadException("No se puede almacenar un archivo vacío: " + originalFileName); //
            }
            if (uniqueFileName.contains("..")) { //
                throw new FileUploadException("Secuencia inválida en nombre de archivo: " + uniqueFileName); //
            }

            Path targetLocation = this.fileStorageLocationAbs.resolve(uniqueFileName); //
            try (InputStream inputStream = file.getInputStream()) { //
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING); //
            }
            return uniqueFileName; //
        } catch (IOException ex) { //
            throw new FileUploadException("No se pudo almacenar el archivo " + originalFileName + ". Por favor, inténtalo de nuevo.", ex); //
        }
    }

    @Transactional
    public void deletePlaylist(Long id) { //
        PlaylistEntity playlist = playlistRepository.findById(id) //
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + id));

        if (playlist.getCoverImageUrl() != null && !playlist.getCoverImageUrl().isEmpty()) { //
            try { //
                String fileName = Paths.get(playlist.getCoverImageUrl()).getFileName().toString(); //
                Path filePath = this.fileStorageLocationAbs.resolve(fileName); //
                Files.deleteIfExists(filePath); //
            } catch (IOException e) { //
                System.err.println("Error al eliminar archivo de portada: " + playlist.getCoverImageUrl() + " - " + e.getMessage()); //
            }
        }
        playlistRepository.deleteById(id); //
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) { //
        PlaylistEntity playlist = playlistRepository.findById(playlistId) //
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + playlistId));
        SongEntity song = songRepository.findById(songId) //
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada con ID: " + songId));

        if (playlist.getSongs().stream().anyMatch(s -> s.getId().equals(songId))) { //
            throw new RuntimeException("La canción ya está en la playlist");
        }
        playlist.getSongs().add(song); //
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) { //
        PlaylistEntity playlist = playlistRepository.findById(playlistId) //
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + playlistId));
        SongEntity songToRemove = playlist.getSongs().stream() //
                .filter(s -> s.getId().equals(songId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("La canción no está en la playlist"));
        playlist.getSongs().remove(songToRemove); //
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPlaylistsByOwner(Long ownerId) { //
        UserEntity owner = userRepository.findById(ownerId) //
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + ownerId));
        List<PlaylistEntity> playlists = playlistRepository.findByOwner(owner); //
        return playlists.stream() //
                .map(playlistMapper::toPlaylistResponse) //
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylistsByOwner(Long ownerId) { //
        UserEntity owner = userRepository.findById(ownerId) //
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + ownerId));
        List<PlaylistEntity> playlists = playlistRepository.findByOwnerAndIsPublicTrue(owner); //
        return playlists.stream() //
                .map(playlistMapper::toPlaylistResponse) //
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylists() { //
        List<PlaylistEntity> playlists = playlistRepository.findByIsPublicTrue(); //
        return playlists.stream() //
                .map(playlistMapper::toPlaylistResponse) //
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) { //
        return userRepository.findByEmail(email) //
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email))
                .getId();
    }
}