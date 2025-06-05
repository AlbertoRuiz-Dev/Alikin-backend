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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final SongRepository songRepository;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;

    @Value("${app.base-url:http://localhost:8080}") // Proporciona un valor por defecto
    private String appBaseUrl;

    private String buildFullFileUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Asegura que no haya dobles barras y que el prefijo sea correcto
        String uploadsPrefix = "/uploads/";
        if (relativePath.startsWith(uploadsPrefix)) { // Si FileStorageService ya incluye /uploads/
            return appBaseUrl + (relativePath.startsWith("/") ? "" : "/") + relativePath;
        }
        return appBaseUrl + uploadsPrefix + (relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
    }

    @Transactional
    public PostResponse createPostInCommunityAndHandleFiles(Long communityId, PostRequest postRequest, MultipartFile imageFile, MultipartFile songFile, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        CommunityEntity community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId));

        if (!community.getMembers().contains(user)) {
            throw new RuntimeException("Debes ser miembro de la comunidad para publicar en ella.");
        }

        PostEntity post = new PostEntity();
        post.setUser(user);
        post.setCommunity(community);
        post.setContent(postRequest.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setVoteCount(0);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String relativeImagePath = fileStorageService.storeFile(imageFile, "post-images");
                post.setImageUrl(buildFullFileUrl(relativeImagePath));
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la imagen del post: " + e.getMessage(), e);
            }
        } else if (postRequest.getImageUrl() != null && !postRequest.getImageUrl().isEmpty()){
            post.setImageUrl(postRequest.getImageUrl());
        }

        if (songFile != null && !songFile.isEmpty()) {
            try {
                String relativeSongPath = fileStorageService.storeFile(songFile, "post-songs");
                SongEntity newSong = new SongEntity();
                String originalFilename = songFile.getOriginalFilename();
                newSong.setTitle(originalFilename != null ? originalFilename.replaceFirst("[.][^.]+$", "") : "Canción Subida");
                newSong.setArtist(user.getNickname());
                newSong.setUrl(buildFullFileUrl(relativeSongPath)); // Usamos el campo 'url' para la URL pública del archivo
                newSong.setUploader(user);
                newSong.setUploadedAt(LocalDateTime.now());
                SongEntity savedSong = songRepository.save(newSong);
                post.setSong(savedSong);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar el archivo de canción: " + e.getMessage(), e);
            }
        } else if (postRequest.getSongId() != null) {
            SongEntity song = songRepository.findById(postRequest.getSongId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canción existente no encontrada con ID: " + postRequest.getSongId()));
            post.setSong(song);
        }

        PostEntity savedPost = postRepository.save(post);
        return postMapper.toPostResponse(savedPost);
    }

    @Transactional
    public PostResponse createGeneralPost(PostRequest postRequest, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        PostEntity post = postMapper.toEntity(postRequest);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        // voteCount y commentsCount se inicializan a 0 en PostEntity o el mapper los calcula/setea

        if (postRequest.getCommunityId() != null) {
            CommunityEntity community = communityRepository.findById(postRequest.getCommunityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comunidad no encontrada con ID: " + postRequest.getCommunityId()));
            post.setCommunity(community);
        }

        if (postRequest.getSongId() != null) {
            SongEntity song = songRepository.findById(postRequest.getSongId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada con ID: " + postRequest.getSongId()));
            post.setSong(song);
        }

        if (postRequest.getImageUrl() != null && !postRequest.getImageUrl().isEmpty()) {
            post.setImageUrl(postRequest.getImageUrl());
        }

        PostEntity savedPost = postRepository.save(post);
        return postMapper.toPostResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + id));
        return postMapper.toPostResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        Page<PostEntity> posts = postRepository.findByUser(user, pageable);
        Page<PostResponse> responsePage = posts.map(postMapper::toPostResponse);
        setUserVoteStatusForPage(responsePage, userId);
        return responsePage;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getCommunityPosts(Long communityId, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }
        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Comunidad no encontrada con ID: " + communityId);
        }
        Page<PostEntity> posts = postRepository.findByCommunityIdOrderByCreatedAtDesc(communityId, pageable);
        Page<PostResponse> responsePage = posts.map(postMapper::toPostResponse);
        return responsePage;
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest postRequest) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada para actualizar con ID: " + id));
        postMapper.updatePostFromRequest(postRequest, post);
        PostEntity updatedPost = postRepository.save(post);
        return postMapper.toPostResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada para eliminar con ID: " + id));

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty() && !post.getImageUrl().toLowerCase().startsWith("http")) {
            // Si no es una URL completa, podría ser una ruta relativa gestionada por FileStorageService
            // Necesitarías extraer la parte relativa de la URL completa para pasarla a deleteFile
            String relativePath = extractRelativePath(post.getImageUrl());
            if (relativePath != null) {
                try {
                    fileStorageService.deleteFile(relativePath);
                } catch (Exception e) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo de imagen " + relativePath + " para el post " + id + ": " + e.getMessage());
                }
            }
        }
        if (post.getSong() != null && post.getSong().getUrl() != null && !post.getSong().getUrl().isEmpty() && !post.getSong().getUrl().toLowerCase().startsWith("http")) {
            String relativePath = extractRelativePath(post.getSong().getUrl());
            if (relativePath != null) {
                try {
                    fileStorageService.deleteFile(relativePath);
                } catch (Exception e) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo de canción " + relativePath + " para el post " + id + ": " + e.getMessage());
                }
            }
        }
        postRepository.deleteById(id);
    }

    private String extractRelativePath(String fullUrl) {
        if (fullUrl == null) return null;
        String prefix = appBaseUrl + "/uploads/";
        if (fullUrl.startsWith(prefix)) {
            return fullUrl.substring(prefix.length());
        }
        return null; // O devuelve fullUrl si no es un path gestionado localmente
    }

    @Transactional
    public PostResponse votePost(Long postId, Long userId, int voteValue) {
        if (voteValue != 1 && voteValue != -1 && voteValue != 0) {
            throw new IllegalArgumentException("Valor de voto inválido: " + voteValue);
        }
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + postId));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Integer previousVote = post.getUserVotes().get(user);
        int voteChange = 0;
        int finalUserVote = voteValue;

        if (voteValue == 0) {
            if (previousVote != null) {
                voteChange = -previousVote;
                post.getUserVotes().remove(user);
            }
        } else {
            if (previousVote == null) {
                voteChange = voteValue;
            } else {
                if (previousVote.equals(voteValue)) {
                    voteChange = -previousVote;
                    post.getUserVotes().remove(user);
                    finalUserVote = 0;
                } else {
                    voteChange = voteValue - previousVote;
                }
            }
            if(finalUserVote != 0) {
                post.getUserVotes().put(user, finalUserVote);
            }
        }
        post.setVoteCount(post.getVoteCount() + voteChange);
        PostEntity updatedPost = postRepository.save(post);
        PostResponse response = postMapper.toPostResponse(updatedPost);
        response.setUserVote(finalUserVote);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedForUser(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Aplicar sort por defecto si no viene uno del cliente
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }

        List<UserEntity> following = new ArrayList<>(user.getFollowing()); // Convertir a lista para el repo
        List<CommunityEntity> joinedCommunitiesList = new ArrayList<>(user.getCommunities());

        Page<PostEntity> posts;
        // Lógica para determinar qué posts mostrar (combinados, solo de seguidos, solo de comunidades, o todos)
        // Esta es la parte más compleja y depende de tus reglas de negocio para el "feed personalizado".
        // Ejemplo simplificado: si no sigue a nadie ni está en comunidades, muestra todos los posts ordenados.
        // Si sigue o está en comunidades, necesitarías una query más compleja en el repositorio.

        if (following.isEmpty() && joinedCommunitiesList.isEmpty()) {
            // Ya usa el método del repo que ordena, pero el pageable ahora SÍ tiene el sort.
            posts = postRepository.findByOrderByCreatedAtDesc(pageable);
        } else {
            // Aquí necesitarías una lógica más elaborada y posiblemente un método de repositorio personalizado
            // que pueda filtrar por usuarios seguidos Y/O comunidades a las que pertenece,
            // y que acepte el Pageable con el Sort.
            // Ejemplo:
            // posts = postRepository.findFeedPostsForUser(following, joinedCommunitiesList, pageable);
            // Por ahora, como fallback, si esta lógica es compleja, usamos el general ordenado:
            // ESTO ES UN FALLBACK, DEBERÍAS IMPLEMENTAR LA LÓGICA REAL DEL FEED PERSONALIZADO
            posts = postRepository.findByUserInOrCommunityInOrderByCreatedAtDesc(following, joinedCommunitiesList, pageable);

            // Si posts está vacío después de filtrar por "following" y "communities",
            // podrías decidir mostrar posts generales como fallback, o nada.
            if (posts.isEmpty() && (pageable.getPageNumber() == 0)) { // Solo para la primera página del feed personalizado

            }

        }

        Page<PostResponse> responsePage = posts.map(postMapper::toPostResponse);
        setUserVoteStatusForPage(responsePage, userId);
        return responsePage;
    }

    @Transactional(readOnly = true)
    public void setUserVoteStatus(PostResponse postResponse, Long userId) {
        if (postResponse == null || postResponse.getId() == null || userId == null) {
            if(postResponse != null) postResponse.setUserVote(0);
            return;
        }
        userRepository.findById(userId).ifPresent(user -> {
            postRepository.findById(postResponse.getId()).ifPresent(postEntity -> {
                Integer voteValue = postEntity.getUserVotes().get(user);
                postResponse.setUserVote(voteValue != null ? voteValue : 0);
            });
            if(postRepository.findById(postResponse.getId()).isEmpty()){
                postResponse.setUserVote(0);
            }
        });
        if(userRepository.findById(userId).isEmpty()){
            postResponse.setUserVote(0);
        }
    }

    @Transactional(readOnly = true)
    public void setUserVoteStatusForPage(Page<PostResponse> postsPage, Long userId) {
        if (postsPage == null || postsPage.getContent().isEmpty() || userId == null) {
            if (postsPage != null) postsPage.getContent().forEach(p -> p.setUserVote(0));
            return;
        }
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            postsPage.getContent().forEach(p -> p.setUserVote(0));
            return;
        }

        List<Long> postIds = postsPage.getContent().stream()
                .map(PostResponse::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (postIds.isEmpty()) return;

        List<PostEntity> postEntities = postRepository.findAllById(postIds);
        java.util.Map<Long, PostEntity> postEntityMap = postEntities.stream()
                .collect(Collectors.toMap(PostEntity::getId, pe -> pe));

        for (PostResponse postDto : postsPage.getContent()) {
            PostEntity postEntity = postEntityMap.get(postDto.getId());
            if (postEntity != null && postEntity.getUserVotes() != null) { // Añadido check para userVotes no nulo
                Integer voteValue = postEntity.getUserVotes().get(user);
                postDto.setUserVote(voteValue != null ? voteValue : 0);
            } else {
                postDto.setUserVote(0);
            }
        }
    }

    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email))
                .getId();
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getGlobalPostsWithoutCommunity(Pageable pageable, Long currentUserId) {
        Page<PostEntity> postEntities = postRepository.findGlobalPostsWithoutCommunity(pageable);
        // Mapea PostEntity a PostResponse. Asumo que tienes un mapper.
        // Si tu PostResponse necesita información del owner, asegúrate que se cargue (JOIN FETCH en el repo o acceso en transacción)
        Page<PostResponse> postResponses = postEntities.map(postMapper::toPostResponse); // Asumiendo un postMapper

        // Opcional pero recomendado: establece el estado de voto del usuario actual para estos posts
        if (currentUserId != null) {
            setUserVoteStatusForPage(postResponses, currentUserId);
        }
        return postResponses;
    }
}