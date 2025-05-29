package com.backendalikin.service;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.CommunityEntity;
import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.enums.CommunityRole;
import com.backendalikin.mapper.CommunityMapper;
import com.backendalikin.mapper.UserMapper;
import com.backendalikin.repository.CommunityRepository;
import com.backendalikin.repository.PlaylistRepository;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final CommunityMapper communityMapper;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public CommunityResponse createCommunity(CommunityRequest communityRequestData, MultipartFile imageFile, Long userId) {
        if (communityRepository.existsByName(communityRequestData.getName())) {
            throw new RuntimeException("Ya existe una comunidad con ese nombre: " + communityRequestData.getName());
        }

        UserEntity leader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario líder no encontrado con ID: " + userId));

        String storedImagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                storedImagePath = fileStorageService.storeFile(imageFile, "community-images");
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen de la comunidad: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                throw new RuntimeException("Error al procesar el archivo de imagen: " + e.getMessage(), e);
            }
        }

        CommunityEntity community = communityMapper.toEntity(communityRequestData);
        community.setCreatedAt(LocalDateTime.now());
        community.setLeader(leader);

        if (storedImagePath != null) {
            community.setImageUrl(storedImagePath);
        }

        community.setMembers(new HashSet<>());
        community.getMembers().add(leader);

        community.setUserRoles(new HashMap<>());
        community.getUserRoles().put(leader, CommunityRole.LEADER);

        CommunityEntity savedCommunity = communityRepository.save(community);
        CommunityResponse response = communityMapper.toCommunityResponse(savedCommunity);
        response.setMember(true);
        response.setUserRole(CommunityRole.LEADER.name());
        return response;
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityById(Long id) {
        CommunityEntity community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + id));
        return communityMapper.toCommunityResponse(community);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> searchCommunities(String name) {
        List<CommunityEntity> communities = communityRepository.findByNameContainingIgnoreCase(name);
        return communities.stream()
                .map(communityMapper::toCommunityResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getUserCommunities(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        List<CommunityEntity> communities = communityRepository.findCommunitiesByMember(user);
        return communities.stream()
                .map(community -> {
                    CommunityResponse response = communityMapper.toCommunityResponse(community);
                    response.setMember(true);
                    CommunityRole role = community.getUserRoles().get(user);
                    if (role != null) {
                        response.setUserRole(role.name());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommunityResponse updateCommunityWithImage(Long id, CommunityRequest communityRequestData, MultipartFile imageFile) {
        CommunityEntity community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada para actualizar con ID: " + id));

        // Actualizar nombre y descripción desde el DTO
        if (communityRequestData.getName() != null && !communityRequestData.getName().isEmpty()) {
            // Considera validar si el nuevo nombre ya existe y no es la misma comunidad
            if (!community.getName().equalsIgnoreCase(communityRequestData.getName()) &&
                    communityRepository.existsByName(communityRequestData.getName())) {
                throw new RuntimeException("Ya existe otra comunidad con el nombre: " + communityRequestData.getName());
            }
            community.setName(communityRequestData.getName());
        }
        if (communityRequestData.getDescription() != null) {
            community.setDescription(communityRequestData.getDescription());
        }

        // Manejar la actualización de la imagen
        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImagePath = community.getImageUrl(); // Guardar la ruta de la imagen antigua

            // Guardar la nueva imagen
            String newImagePath;
            try {
                newImagePath = fileStorageService.storeFile(imageFile, "community-images");
                community.setImageUrl(newImagePath); // Actualizar la URL de la imagen en la entidad
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la nueva imagen de la comunidad: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                throw new RuntimeException("Error al procesar el nuevo archivo de imagen: " + e.getMessage(), e);
            }

            // Si había una imagen antigua y es diferente de la nueva (y no es una URL por defecto), eliminarla
            if (oldImagePath != null && !oldImagePath.isEmpty() && !oldImagePath.equals(newImagePath)) {
                // Asegúrate de que fileStorageService.deleteFile no lance una excepción fatal
                // si el archivo no existe, o manéjalo adecuadamente.
                try {
                    fileStorageService.deleteFile(oldImagePath);
                } catch (Exception e) {
                    // Loggear la advertencia pero continuar, ya que la nueva imagen se guardó.
                    System.err.println("Advertencia: No se pudo eliminar la imagen antigua: " + oldImagePath + ". Error: " + e.getMessage());
                }
            }
        }
        // Si imageFile es null pero se quiere explícitamente quitar la imagen (ej. un checkbox "eliminar imagen actual")
        // podrías añadir lógica aquí para setear community.setImageUrl(null) y borrar el archivo existente.
        // Por ahora, si imageFile es null, no se modifica la imagen existente.

        CommunityEntity updatedCommunity = communityRepository.save(community);
        return communityMapper.toCommunityResponse(updatedCommunity);
    }


    @Transactional
    public void deleteCommunity(Long id) {
        CommunityEntity communityEntity = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada para eliminar con ID: " + id));

        if (communityEntity.getImageUrl() != null && !communityEntity.getImageUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(communityEntity.getImageUrl());
            } catch (RuntimeException e) {
                System.err.println("Advertencia: No se pudo eliminar el archivo de imagen " + communityEntity.getImageUrl() + " para la comunidad " + id + ": " + e.getMessage());
            }
        }
        communityRepository.deleteById(id);
    }

    @Transactional
    public void joinCommunity(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (community.getMembers().contains(user)) {
            throw new RuntimeException("Ya eres miembro de esta comunidad");
        }

        community.getMembers().add(user);
        community.getUserRoles().put(user, CommunityRole.MEMBER);

        communityRepository.save(community);
    }

    @Transactional
    public void leaveCommunity(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (community.getLeader().getId().equals(userId)) {
            if (community.getMembers().size() > 1) {
                throw new RuntimeException("El líder no puede abandonar la comunidad si hay otros miembros. Transfiere el liderazgo primero.");
            } else {
                this.deleteCommunity(communityId);
                return;
            }
        }

        if (!community.getMembers().contains(user)) {
            throw new RuntimeException("No eres miembro de esta comunidad");
        }

        community.getMembers().remove(user);
        community.getUserRoles().remove(user);

        communityRepository.save(community);
    }

    @Transactional
    public void setCommunityRadio(Long communityId, Long playlistId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist no encontrada con ID: " + playlistId));

        community.setRadioPlaylist(playlist);
        communityRepository.save(community);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getCommunityMembers(Long communityId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        return community.getMembers().stream()
                .map(userEntity -> {
                    UserResponse userResponse = userMapper.toUserResponse(userEntity);
                    CommunityRole role = community.getUserRoles().get(userEntity);
                    if (role != null) {
                        userResponse.setRole(role.name());
                    }
                    return userResponse;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void setMembershipStatus(CommunityResponse communityResponse, Long userId) {
        if (communityResponse == null || communityResponse.getId() == null) return;

        CommunityEntity community = communityRepository.findById(communityResponse.getId()).orElse(null);
        if (community == null) {
            communityResponse.setMember(false);
            communityResponse.setUserRole(null);
            return;
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            communityResponse.setMember(false);
            communityResponse.setUserRole(null);
            return;
        }

        boolean isMember = community.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        communityResponse.setMember(isMember);

        if (isMember) {
            CommunityRole role = community.getUserRoles().get(user);
            if (role != null) {
                communityResponse.setUserRole(role.name());
            } else {
                communityResponse.setUserRole(CommunityRole.MEMBER.name());
            }
        } else {
            communityResponse.setUserRole(null);
        }
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email))
                .getId();
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> getAllCommunities() {
        List<CommunityEntity> communities = communityRepository.findAll();
        return communities.stream()
                .map(communityMapper::toCommunityResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommunityResponse setCommunityRadioStation(Long communityId, String stationName, String streamUrl, String stationLogoUrl) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));
        community.setRadioStationName(stationName);
        community.setRadioStreamUrl(streamUrl);
        community.setRadioStationLogoUrl(stationLogoUrl); // puede ser null

        CommunityEntity updatedCommunity = communityRepository.save(community);
        return communityMapper.toCommunityResponse(updatedCommunity);
    }

    @Transactional
    public void kickMember(Long communityId, Long memberToKickId, Long leaderId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        UserEntity memberToKick = userRepository.findById(memberToKickId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a expulsar no encontrado con ID: " + memberToKickId));
        community.getMembers().remove(memberToKick);
        community.getUserRoles().remove(memberToKick);

        communityRepository.save(community);
    }

}