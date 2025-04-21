package com.backendalikin.service;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.CommunityEntity;
import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.CommunityMapper;
import com.backendalikin.mapper.UserMapper;
import com.backendalikin.model.enums.CommunityRole;
import com.backendalikin.repository.CommunityRepository;
import com.backendalikin.repository.PlaylistRepository;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public CommunityResponse createCommunity(CommunityRequest communityRequest, Long userId) {
        if (communityRepository.existsByName(communityRequest.getName())) {
            throw new RuntimeException("Ya existe una comunidad con ese nombre");
        }
        
        UserEntity leader = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        CommunityEntity community = communityMapper.toEntity(communityRequest);
        community.setCreatedAt(LocalDateTime.now());
        community.setLeader(leader);
        
        // Inicializar miembros y roles
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
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<CommunityEntity> communities = communityRepository.findCommunitiesByMember(user);
        return communities.stream()
                .map(community -> {
                    CommunityResponse response = communityMapper.toCommunityResponse(community);
                    response.setMember(true);
                    response.setUserRole(community.getUserRoles().get(user).name());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommunityResponse updateCommunity(Long id, CommunityRequest communityRequest) {
        CommunityEntity community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        communityMapper.updateCommunityFromRequest(communityRequest, community);
        
        CommunityEntity updatedCommunity = communityRepository.save(community);
        return communityMapper.toCommunityResponse(updatedCommunity);
    }

    @Transactional
    public void deleteCommunity(Long id) {
        if (!communityRepository.existsById(id)) {
            throw new RuntimeException("Comunidad no encontrada");
        }
        communityRepository.deleteById(id);
    }

    @Transactional
    public void joinCommunity(Long communityId, Long userId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
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
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (community.getLeader().getId().equals(userId)) {
            throw new RuntimeException("El líder no puede abandonar la comunidad");
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
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        community.setRadioPlaylist(playlist);
        communityRepository.save(community);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getCommunityMembers(Long communityId) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        return community.getMembers().stream()
                .map(userMapper::toUserResponse)
                .peek(user -> user.setRole(community.getUserRoles().get(userRepository.findById(user.getId()).get()).name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void setMembershipStatus(CommunityResponse communityResponse, Long userId) {
        CommunityEntity community = communityRepository.findById(communityResponse.getId())
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        boolean isMember = community.getMembers().contains(user);
        communityResponse.setMember(isMember);
        
        if (isMember) {
            communityResponse.setUserRole(community.getUserRoles().get(user).name());
        }
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email))
                .getId();
    }
}