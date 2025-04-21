package com.backendalikin.service;

import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.UserMapper;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email))
                .getId();
    }

    @Transactional(readOnly = true)
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("No hay usuario autenticado");
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        userMapper.updateUserFromRequest(updateRequest, user);
        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("Un usuario no puede seguirse a sí mismo");
        }
        
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Usuario seguidor no encontrado"));
        
        UserEntity followed = userRepository.findById(followedId)
                .orElseThrow(() -> new RuntimeException("Usuario a seguir no encontrado"));
        
        if (follower.getFollowing().contains(followed)) {
            throw new RuntimeException("Ya sigues a este usuario");
        }
        
        follower.getFollowing().add(followed);
        userRepository.save(follower);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Usuario seguidor no encontrado"));
        
        UserEntity followed = userRepository.findById(followedId)
                .orElseThrow(() -> new RuntimeException("Usuario a dejar de seguir no encontrado"));
        
        if (!follower.getFollowing().contains(followed)) {
            throw new RuntimeException("No sigues a este usuario");
        }
        
        follower.getFollowing().remove(followed);
        userRepository.save(follower);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserFollowers(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        
        return user.getFollowers().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUserFollowing(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        
        return user.getFollowing().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void checkIfFollowing(String email, Long targetUserId, UserResponse targetUserResponse) {
        UserEntity currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));
        
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario objetivo no encontrado"));
        
        targetUserResponse.setFollowing(currentUser.getFollowing().contains(targetUser));
    }
}