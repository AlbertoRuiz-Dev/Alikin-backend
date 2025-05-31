package com.backendalikin.service;

import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.UserMapper;
import com.backendalikin.repository.UserRepository;
import com.backendalikin.service.FileStorageService; // Actualizado
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email))
                .getId();
    }

    @Transactional(readOnly = true)
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new EntityNotFoundException("No hay usuario autenticado o información de principal inválida");
        }
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return getUserIdByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByNickname(String nickname) {
        List<UserEntity> users = userRepository.findByNicknameContainingIgnoreCase(nickname);
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        userMapper.updateUserFromRequest(updateRequest, user);
        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(user.getProfilePictureUrl());
            } catch (RuntimeException e) {
                System.err.println("Error al eliminar el archivo de avatar durante la eliminación del usuario " + id + ": " + e.getMessage());
            }
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo");
        }

        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario seguidor no encontrado con ID: " + followerId));

        UserEntity followed = userRepository.findById(followedId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario a seguir no encontrado con ID: " + followedId));

        if (follower.getFollowing().contains(followed)) {
            throw new IllegalStateException("Ya sigues a este usuario");
        }

        follower.getFollowing().add(followed);
        userRepository.save(follower);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario seguidor no encontrado con ID: " + followerId));

        UserEntity followed = userRepository.findById(followedId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario a dejar de seguir no encontrado con ID: " + followedId));

        if (!follower.getFollowing().contains(followed)) {
            throw new IllegalStateException("No sigues a este usuario");
        }

        follower.getFollowing().remove(followed);
        userRepository.save(follower);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserFollowers(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        return user.getFollowers().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserFollowing(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        return user.getFollowing().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void checkIfFollowing(String email, Long targetUserId, UserResponse targetUserResponse) {
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario actual no encontrado con email: " + email));

        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario objetivo no encontrado con ID: " + targetUserId));

        targetUserResponse.setFollowing(currentUser.getFollowing().contains(targetUser));
    }

    @Transactional
    public String updateUserAvatar(Long userId, MultipartFile avatarFile) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(user.getProfilePictureUrl());
            } catch (RuntimeException e) {
                System.err.println("Error al eliminar avatar antiguo del usuario " + userId + ": " + e.getMessage());
            }
        }

        String storedFilePath;
        try {
            String subdirectory = "user_avatars/" + userId; // Subdirectorio específico para avatares de este usuario
            storedFilePath = fileStorageService.storeFile(avatarFile, subdirectory);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo de avatar para el usuario " + userId + ": " + e.getMessage(), e);
        }

        user.setProfilePictureUrl(storedFilePath);
        userRepository.save(user);
        return storedFilePath;
    }

    @Transactional
    public UserResponse removeUserAvatar(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(user.getProfilePictureUrl());
            } catch (RuntimeException e) {
                System.err.println("Error al eliminar avatar del usuario " + userId + ": " + e.getMessage());
            }
        }

        user.setProfilePictureUrl(null);
        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }
}