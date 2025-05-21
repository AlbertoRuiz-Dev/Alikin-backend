package com.backendalikin.mapper;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityBasicResponse;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.entity.CommunityEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class CommunityMapperImpl implements CommunityMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PlaylistMapper playlistMapper;

    @Override
    public CommunityResponse toCommunityResponse(CommunityEntity entity) {
        if ( entity == null ) {
            return null;
        }

        CommunityResponse.CommunityResponseBuilder communityResponse = CommunityResponse.builder();

        communityResponse.leader( userMapper.toUserBasicResponse( entity.getLeader() ) );
        communityResponse.radioPlaylist( playlistMapper.toPlaylistBasicResponse( entity.getRadioPlaylist() ) );
        communityResponse.id( entity.getId() );
        communityResponse.name( entity.getName() );
        communityResponse.description( entity.getDescription() );
        communityResponse.imageUrl( entity.getImageUrl() );
        communityResponse.createdAt( entity.getCreatedAt() );

        communityResponse.membersCount( entity.getMembers().size() );

        return communityResponse.build();
    }

    @Override
    public CommunityBasicResponse toCommunityBasicResponse(CommunityEntity entity) {
        if ( entity == null ) {
            return null;
        }

        CommunityBasicResponse.CommunityBasicResponseBuilder communityBasicResponse = CommunityBasicResponse.builder();

        communityBasicResponse.id( entity.getId() );
        communityBasicResponse.name( entity.getName() );
        communityBasicResponse.imageUrl( entity.getImageUrl() );

        return communityBasicResponse.build();
    }

    @Override
    public CommunityEntity toEntity(CommunityRequest request) {
        if ( request == null ) {
            return null;
        }

        CommunityEntity.CommunityEntityBuilder communityEntity = CommunityEntity.builder();

        communityEntity.name( request.getName() );
        communityEntity.description( request.getDescription() );
        communityEntity.imageUrl( request.getImageUrl() );

        return communityEntity.build();
    }

    @Override
    public void updateCommunityFromRequest(CommunityRequest request, CommunityEntity entity) {
        if ( request == null ) {
            return;
        }

        entity.setDescription( request.getDescription() );
        entity.setImageUrl( request.getImageUrl() );
    }
}
