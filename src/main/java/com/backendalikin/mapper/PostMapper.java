package com.backendalikin.mapper;

import com.backendalikin.dto.request.PostRequest;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.entity.PostEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SongMapper.class, CommunityMapper.class})
public interface PostMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "song", source = "song")
    @Mapping(target = "community", source = "community")
    @Mapping(target = "commentsCount", expression = "java(entity.getComments() != null ? entity.getComments().size() : 0)")
    @Mapping(target = "userVote", ignore = true) // Esto se setea en el servicio
    PostResponse toPostResponse(PostEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "voteCount", ignore = true)
    @Mapping(target = "userVotes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    PostEntity toEntity(PostRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "community", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "voteCount", ignore = true)
    @Mapping(target = "userVotes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updatePostFromRequest(PostRequest request, @MappingTarget PostEntity entity);
}