package com.backendalikin.mapper;

import com.backendalikin.dto.request.CommentRequest;
import com.backendalikin.dto.response.CommentResponse;
import com.backendalikin.entity.CommentEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "user", source = "user")
    CommentResponse toCommentResponse(CommentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CommentEntity toEntity(CommentRequest request);
}