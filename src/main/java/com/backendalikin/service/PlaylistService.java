package com.backendalikin.service;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.SongEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.PlaylistMapper;
import com.backendalikin.repository.PlaylistRepository;
import com.backendalikin.repository.SongRepository;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest playlistRequest, Long userId) {
        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PlaylistEntity playlist = playlistMapper.toEntity(playlistRequest);
        playlist.setOwner(owner);
        playlist.setCreatedAt(LocalDateTime.now());

        // Inicializar lista de canciones
        playlist.setSongs(new ArrayList<>());

        // Agregar canciones si se proporcionan IDs
        if (playlistRequest.getSongIds() != null && !playlistRequest.getSongIds().isEmpty()) {
            for (Long songId : playlistRequest.getSongIds()) {
                SongEntity song = songRepository.findById(songId)
                        .orElseThrow(() -> new RuntimeException("Canción no encontrada con ID: " + songId));
                playlist.getSongs().add(song);
            }
        }

        PlaylistEntity savedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(savedPlaylist);
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long id) {
        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        return playlistMapper.toPlaylistResponse(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long id, PlaylistRequest playlistRequest) {
        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        playlistMapper.updatePlaylistFromRequest(playlistRequest, playlist);

        // Actualizar canciones si se proporcionan
        if (playlistRequest.getSongIds() != null) {
            playlist.getSongs().clear();
            for (Long songId : playlistRequest.getSongIds()) {
                SongEntity song = songRepository.findById(songId)
                        .orElseThrow(() -> new RuntimeException("Canción no encontrada con ID: " + songId));
                playlist.getSongs().add(song);
            }
        }

        PlaylistEntity updatedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(updatedPlaylist);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new RuntimeException("Playlist no encontrada");
        }
        playlistRepository.deleteById(id);
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        SongEntity song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (playlist.getSongs().contains(song)) {
            throw new RuntimeException("La canción ya está en la playlist");
        }

        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        SongEntity song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (!playlist.getSongs().contains(song)) {
            throw new RuntimeException("La canción no está en la playlist");
        }

        playlist.getSongs().remove(song);
        playlistRepository.save(playlist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPlaylistsByOwner(Long ownerId) {
        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<PlaylistEntity> playlists = playlistRepository.findByOwner(owner);
        return playlists.stream()
                .map(playlistMapper::toPlaylistResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylistsByOwner(Long ownerId) {
        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<PlaylistEntity> playlists = playlistRepository.findByOwnerAndIsPublicTrue(owner);
        return playlists.stream()
                .map(playlistMapper::toPlaylistResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylists() {
        List<PlaylistEntity> playlists = playlistRepository.findByIsPublicTrue();
        return playlists.stream()
                .map(playlistMapper::toPlaylistResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email))
                .getId();
    }
}