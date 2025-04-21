package com.backendalikin.controller;

import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        UserResponse userResponse = userService.getUserById(id);
        // Comprobar si el usuario actual sigue a este usuario
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            userService.checkIfFollowing(email, id, userResponse);
        }
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == @userService.getUserIdFromAuthentication(authentication) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<MessageResponse> followUser(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long currentUserId = userService.getUserIdByEmail(email);
        userService.followUser(currentUserId, id);
        return ResponseEntity.ok(new MessageResponse("Usuario seguido correctamente"));
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<MessageResponse> unfollowUser(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long currentUserId = userService.getUserIdByEmail(email);
        userService.unfollowUser(currentUserId, id);
        return ResponseEntity.ok(new MessageResponse("Ha dejado de seguir al usuario correctamente"));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserResponse>> getUserFollowers(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFollowers(id));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserResponse>> getUserFollowing(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFollowing(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsersByNickname(query));
    }
}