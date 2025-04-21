package com.backendalikin.controller;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.dto.response.MessageResponse;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.service.CommunityService;
import com.backendalikin.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final PostService postService;

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityRequest communityRequest,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        return ResponseEntity.ok(communityService.createCommunity(communityRequest, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponse> getCommunityById(
            @PathVariable Long id,
            Authentication authentication) {
        CommunityResponse community = communityService.getCommunityById(id);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = communityService.getUserIdByEmail(email);
            communityService.setMembershipStatus(community, userId);
        }
        return ResponseEntity.ok(community);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable Long id,
            @Valid @RequestBody CommunityRequest communityRequest) {
        return ResponseEntity.ok(communityService.updateCommunity(id, communityRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<MessageResponse> joinCommunity(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        communityService.joinCommunity(id, userId);
        return ResponseEntity.ok(new MessageResponse("Te has unido a la comunidad correctamente"));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<MessageResponse> leaveCommunity(
            @PathVariable Long id,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        communityService.leaveCommunity(id, userId);
        return ResponseEntity.ok(new MessageResponse("Has abandonado la comunidad correctamente"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<UserResponse>> getCommunityMembers(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getCommunityMembers(id));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<Page<PostResponse>> getCommunityPosts(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        Page<PostResponse> posts = postService.getCommunityPosts(id, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, userId);
        }
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{id}/radio")
    @PreAuthorize("@securityService.isCommunityLeader(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> setCommunityRadio(
            @PathVariable Long id,
            @RequestParam Long playlistId) {
        communityService.setCommunityRadio(id, playlistId);
        return ResponseEntity.ok(new MessageResponse("Radio de comunidad actualizada correctamente"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CommunityResponse>> searchCommunities(
            @RequestParam String query,
            Authentication authentication) {
        List<CommunityResponse> communities = communityService.searchCommunities(query);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = communityService.getUserIdByEmail(email);
            communities.forEach(community -> communityService.setMembershipStatus(community, userId));
        }
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/user")
    public ResponseEntity<List<CommunityResponse>> getUserCommunities(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = communityService.getUserIdByEmail(email);
        List<CommunityResponse> communities = communityService.getUserCommunities(userId);
        communities.forEach(community -> community.setMember(true));
        return ResponseEntity.ok(communities);
    }
}