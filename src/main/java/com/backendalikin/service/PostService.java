package com.backendalikin.service;

import com.backendalikin.dto.request.PostRequest;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.entity.CommunityEntity;
import com.backendalikin.entity.PostEntity;
import com.backendalikin.entity.SongEntity;
import com.backendalikin.entity.UserEntity;
import com.backendalikin.mapper.PostMapper;
import com.backendalikin.repository.CommunityRepository;
import com.backendalikin.repository.PostRepository;
import com.backendalikin.repository.SongRepository;
import com.backendalikin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final SongRepository songRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse createPost(PostRequest postRequest, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PostEntity post = postMapper.toEntity(postRequest);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());

        // Asociar comunidad si existe
        if (postRequest.getCommunityId() != null) {
            CommunityEntity community = communityRepository.findById(postRequest.getCommunityId())
                    .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

            // Verificar que el usuario es miembro de la comunidad
            if (!community.getMembers().contains(user)) {
                throw new RuntimeException("Debes ser miembro de la comunidad para publicar");
            }

            post.setCommunity(community);
        }

        // Asociar canción si existe
        if (postRequest.getSongId() != null) {
            SongEntity song = songRepository.findById(postRequest.getSongId())
                    .orElseThrow(() -> new RuntimeException("Canción no encontrada"));
            post.setSong(song);
        }

        PostEntity savedPost = postRepository.save(post);
        return postMapper.toPostResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        return postMapper.toPostResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Page<PostEntity> posts = postRepository.findByUser(user, pageable);
        return posts.map(postMapper::toPostResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getCommunityPosts(Long communityId, Pageable pageable) {
        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        Page<PostEntity> posts = postRepository.findByCommunity(community, pageable);
        return posts.map(postMapper::toPostResponse);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest postRequest) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        postMapper.updatePostFromRequest(postRequest, post);

        PostEntity updatedPost = postRepository.save(post);
        return postMapper.toPostResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Publicación no encontrada");
        }
        postRepository.deleteById(id);
    }

    @Transactional
    public PostResponse votePost(Long postId, Long userId, int voteValue) {
        if (voteValue != 1 && voteValue != -1 && voteValue != 0) {
            throw new RuntimeException("Valor de voto inválido");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener voto anterior si existe
        Integer previousVote = post.getUserVotes().get(user);

        // Calcular cambio en contador de votos
        int voteChange = 0;

        if (previousVote == null) {
            // Nuevo voto
            voteChange = voteValue;
        } else if (voteValue == 0) {
            // Quitar voto
            voteChange = -previousVote;
        } else {
            // Cambiar voto
            voteChange = voteValue - previousVote;
        }

        // Actualizar contador de votos
        post.setVoteCount(post.getVoteCount() + voteChange);

        // Actualizar o eliminar registro de voto
        if (voteValue == 0) {
            post.getUserVotes().remove(user);
        } else {
            post.getUserVotes().put(user, voteValue);
        }

        PostEntity updatedPost = postRepository.save(post);
        PostResponse response = postMapper.toPostResponse(updatedPost);
        response.setUserVote(voteValue);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedForUser(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserEntity> following = user.getFollowing().stream().collect(Collectors.toList());
        if (following.isEmpty()) {
            // Si el usuario no sigue a nadie, mostrar posts populares
            return postRepository.findByOrderByVoteCountDesc(pageable)
                    .map(postMapper::toPostResponse);
        }

        // Mostrar posts de usuarios seguidos
        return postRepository.findByUserIn(following, pageable)
                .map(postMapper::toPostResponse);
    }

    @Transactional(readOnly = true)
    public void setUserVoteStatus(PostResponse post, Long userId) {
        PostEntity postEntity = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer voteValue = postEntity.getUserVotes().get(user);
        post.setUserVote(voteValue != null ? voteValue : 0);
    }

    @Transactional(readOnly = true)
    public void setUserVoteStatusForPage(Page<PostResponse> posts, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        for (PostResponse post : posts.getContent()) {
            PostEntity postEntity = postRepository.findById(post.getId())
                    .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

            Integer voteValue = postEntity.getUserVotes().get(user);
            post.setUserVote(voteValue != null ? voteValue : 0);
        }
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email))
                .getId();
    }
}