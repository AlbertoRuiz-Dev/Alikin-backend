package com.backendalikin.mapper;

import com.backendalikin.dto.request.SongRequest;
import com.backendalikin.dto.response.GenreResponse;
import com.backendalikin.dto.response.SongBasicResponse;
import com.backendalikin.dto.response.SongResponse;
import com.backendalikin.entity.GenreEntity;
import com.backendalikin.entity.SongEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class SongMapperImpl implements SongMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GenreMapper genreMapper;

    @Override
    public SongResponse toSongResponse(SongEntity entity) {
        if ( entity == null ) {
            return null;
        }

        SongResponse.SongResponseBuilder songResponse = SongResponse.builder();

        songResponse.uploader( userMapper.toUserBasicResponse( entity.getUploader() ) );
        songResponse.genres( genreEntitySetToGenreResponseList( entity.getGenres() ) );
        songResponse.id( entity.getId() );
        songResponse.title( entity.getTitle() );
        songResponse.artist( entity.getArtist() );
        songResponse.album( entity.getAlbum() );
        songResponse.url( entity.getUrl() );
        songResponse.coverImageUrl( entity.getCoverImageUrl() );
        songResponse.duration( entity.getDuration() );
        songResponse.uploadedAt( entity.getUploadedAt() );

        return songResponse.build();
    }

    @Override
    public SongBasicResponse toSongBasicResponse(SongEntity entity) {
        if ( entity == null ) {
            return null;
        }

        SongBasicResponse.SongBasicResponseBuilder songBasicResponse = SongBasicResponse.builder();

        songBasicResponse.id( entity.getId() );
        songBasicResponse.title( entity.getTitle() );
        songBasicResponse.artist( entity.getArtist() );
        songBasicResponse.coverImageUrl( entity.getCoverImageUrl() );
        songBasicResponse.duration( entity.getDuration() );

        return songBasicResponse.build();
    }

    @Override
    public SongEntity toEntity(SongRequest request) {
        if ( request == null ) {
            return null;
        }

        SongEntity.SongEntityBuilder songEntity = SongEntity.builder();

        songEntity.title( request.getTitle() );
        songEntity.artist( request.getArtist() );
        songEntity.album( request.getAlbum() );

        return songEntity.build();
    }

    @Override
    public void updateSongFromRequest(SongRequest request, SongEntity entity) {
        if ( request == null ) {
            return;
        }

        entity.setTitle( request.getTitle() );
        entity.setArtist( request.getArtist() );
        entity.setAlbum( request.getAlbum() );
    }

    protected List<GenreResponse> genreEntitySetToGenreResponseList(Set<GenreEntity> set) {
        if ( set == null ) {
            return null;
        }

        List<GenreResponse> list = new ArrayList<GenreResponse>( set.size() );
        for ( GenreEntity genreEntity : set ) {
            list.add( genreMapper.toGenreResponse( genreEntity ) );
        }

        return list;
    }
}
