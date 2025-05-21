package com.backendalikin.mapper;

import com.backendalikin.dto.request.CommentRequest;
import com.backendalikin.dto.response.CommentResponse;
import com.backendalikin.entity.CommentEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public CommentResponse toCommentResponse(CommentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        CommentResponse.CommentResponseBuilder commentResponse = CommentResponse.builder();

        commentResponse.user( userMapper.toUserBasicResponse( entity.getUser() ) );
        commentResponse.id( entity.getId() );
        commentResponse.content( entity.getContent() );
        commentResponse.createdAt( entity.getCreatedAt() );

        return commentResponse.build();
    }

    @Override
    public CommentEntity toEntity(CommentRequest request) {
        if ( request == null ) {
            return null;
        }

        CommentEntity.CommentEntityBuilder commentEntity = CommentEntity.builder();

        commentEntity.content( request.getContent() );

        return commentEntity.build();
    }
}
