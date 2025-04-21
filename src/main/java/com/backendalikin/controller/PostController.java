package com.backendalikin.controller;

import com.backendalikin.dto.request.PostRequest;
import com.backendalikin.dto.response.PostResponse;
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

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest postRequest,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        return ResponseEntity.ok(postService.createPost(postRequest, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long id,
            Authentication authentication) {
        PostResponse post = postService.getPostById(id);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatus(post, userId);
        }
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isPostOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest) {
        return ResponseEntity.ok(postService.updatePost(id, postRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isPostOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PostResponse> votePost(
            @PathVariable Long id,
            @RequestParam int value,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        return ResponseEntity.ok(postService.votePost(id, userId, value));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        Page<PostResponse> posts = postService.getUserPosts(userId, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long currentUserId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, currentUserId);
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<Page<PostResponse>> getCommunityPosts(
            @PathVariable Long communityId,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        Page<PostResponse> posts = postService.getCommunityPosts(communityId, pageable);
        if (authentication != null) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            Long userId = postService.getUserIdByEmail(email);
            postService.setUserVoteStatusForPage(posts, userId);
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Long userId = postService.getUserIdByEmail(email);
        Page<PostResponse> feed = postService.getFeedForUser(userId, pageable);
        postService.setUserVoteStatusForPage(feed, userId);
        return ResponseEntity.ok(feed);
    }
}