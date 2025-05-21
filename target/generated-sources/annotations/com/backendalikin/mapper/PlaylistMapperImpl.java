package com.backendalikin.mapper;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.PlaylistBasicResponse;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.dto.response.SongBasicResponse;
import com.backendalikin.entity.PlaylistEntity;
import com.backendalikin.entity.SongEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class PlaylistMapperImpl implements PlaylistMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SongMapper songMapper;

    @Override
    public PlaylistResponse toPlaylistResponse(PlaylistEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PlaylistResponse.PlaylistResponseBuilder playlistResponse = PlaylistResponse.builder();

        playlistResponse.owner( userMapper.toUserBasicResponse( entity.getOwner() ) );
        playlistResponse.songs( songEntityListToSongBasicResponseList( entity.getSongs() ) );
        playlistResponse.id( entity.getId() );
        playlistResponse.name( entity.getName() );
        playlistResponse.description( entity.getDescription() );
        playlistResponse.coverImageUrl( entity.getCoverImageUrl() );
        playlistResponse.createdAt( entity.getCreatedAt() );

        return playlistResponse.build();
    }

    @Override
    public PlaylistBasicResponse toPlaylistBasicResponse(PlaylistEntity entity) {
        if ( entity == null ) {
            return null;
        }

        PlaylistBasicResponse.PlaylistBasicResponseBuilder playlistBasicResponse = PlaylistBasicResponse.builder();

        playlistBasicResponse.id( entity.getId() );
        playlistBasicResponse.name( entity.getName() );
        playlistBasicResponse.coverImageUrl( entity.getCoverImageUrl() );

        return playlistBasicResponse.build();
    }

    @Override
    public PlaylistEntity toEntity(PlaylistRequest request) {
        if ( request == null ) {
            return null;
        }

        PlaylistEntity.PlaylistEntityBuilder playlistEntity = PlaylistEntity.builder();

        playlistEntity.name( request.getName() );
        playlistEntity.description( request.getDescription() );
        playlistEntity.coverImageUrl( request.getCoverImageUrl() );

        return playlistEntity.build();
    }

    @Override
    public void updatePlaylistFromRequest(PlaylistRequest request, PlaylistEntity entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setDescription( request.getDescription() );
        entity.setCoverImageUrl( request.getCoverImageUrl() );
        entity.setPublic( request.isPublic() );
    }

    protected List<SongBasicResponse> songEntityListToSongBasicResponseList(List<SongEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<SongBasicResponse> list1 = new ArrayList<SongBasicResponse>( list.size() );
        for ( SongEntity songEntity : list ) {
            list1.add( songMapper.toSongBasicResponse( songEntity ) );
        }

        return list1;
    }
}
