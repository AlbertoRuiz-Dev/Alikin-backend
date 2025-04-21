package com.backendalikin.service;

import com.backendalikin.entity.*;
import com.backendalikin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    
    /**
     * Verifica si el usuario autenticado es el propietario de la publicación
     */
    public boolean isPostOwner(Authentication authentication, Long postId) {
        if (authentication == null) return false;
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) return false;
        
        Optional<PostEntity> post = postRepository.findById(postId);
        if (post.isEmpty()) return false;
        
        return post.get().getUser().getId().equals(user.getId());
    }
    
    /**
     * Verifica si el usuario autenticado es el propietario del comentario
     */
    public boolean isCommentOwner(Authentication authentication, Long commentId) {
        if (authentication == null) return false;
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) return false;
        
        Optional<CommentEntity> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) return false;
        
        return comment.get().getUser().getId().equals(user.getId());
    }
    
    /**
     * Verifica si el usuario autenticado es el líder de la comunidad
     */
    public boolean isCommunityLeader(Authentication authentication, Long communityId) {
        if (authentication == null) return false;
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) return false;
        
        Optional<CommunityEntity> community = communityRepository.findById(communityId);
        if (community.isEmpty()) return false;
        
        return community.get().getLeader().getId().equals(user.getId());
    }
    
    /**
     * Verifica si el usuario autenticado es el que subió la canción
     */
    public boolean isSongUploader(Authentication authentication, Long songId) {
        if (authentication == null) return false;
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) return false;
        
        Optional<SongEntity> song = songRepository.findById(songId);
        if (song.isEmpty()) return false;
        
        return song.get().getUploader().getId().equals(user.getId());
    }
    
    /**
     * Verifica si el usuario autenticado es el dueño de la playlist
     */
    public boolean isPlaylistOwner(Authentication authentication, Long playlistId) {
        if (authentication == null) return false;
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) return false;
        
        Optional<PlaylistEntity> playlist = playlistRepository.findById(playlistId);
        if (playlist.isEmpty()) return false;
        
        return playlist.get().getOwner().getId().equals(user.getId());
    }
}