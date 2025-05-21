package com.backendalikin.mapper;

import com.backendalikin.dto.request.PostRequest;
import com.backendalikin.dto.response.PostResponse;
import com.backendalikin.entity.PostEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SongMapper songMapper;
    @Autowired
    private CommunityMapper communityMapper;

    @Override
    public PostResponse toPostResponse(PostEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.user( userMapper.toUserBasicResponse( entity.getUser() ) );
        postResponse.song( songMapper.toSongBasicResponse( entity.getSong() ) );
        postResponse.community( communityMapper.toCommunityBasicResponse( entity.getCommunity() ) );
        postResponse.id( entity.getId() );
        postResponse.content( entity.getContent() );
        postResponse.imageUrl( entity.getImageUrl() );
        postResponse.createdAt( entity.getCreatedAt() );
        postResponse.voteCount( entity.getVoteCount() );

        postResponse.commentsCount( entity.getComments() != null ? entity.getComments().size() : 0 );

        return postResponse.build();
    }

    @Override
    public PostEntity toEntity(PostRequest request) {
        if ( request == null ) {
            return null;
        }

        PostEntity.PostEntityBuilder postEntity = PostEntity.builder();

        postEntity.content( request.getContent() );
        postEntity.imageUrl( request.getImageUrl() );

        return postEntity.build();
    }

    @Override
    public void updatePostFromRequest(PostRequest request, PostEntity entity) {
        if ( request == null ) {
            return;
        }

        entity.setContent( request.getContent() );
        entity.setImageUrl( request.getImageUrl() );
    }
}
