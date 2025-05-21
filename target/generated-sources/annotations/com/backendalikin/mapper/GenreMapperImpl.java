package com.backendalikin.mapper;

import com.backendalikin.dto.request.GenreRequest;
import com.backendalikin.dto.response.GenreResponse;
import com.backendalikin.entity.GenreEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class GenreMapperImpl implements GenreMapper {

    @Override
    public GenreResponse toGenreResponse(GenreEntity entity) {
        if ( entity == null ) {
            return null;
        }

        GenreResponse.GenreResponseBuilder genreResponse = GenreResponse.builder();

        genreResponse.id( entity.getId() );
        genreResponse.name( entity.getName() );

        return genreResponse.build();
    }

    @Override
    public GenreEntity toEntity(GenreRequest request) {
        if ( request == null ) {
            return null;
        }

        GenreEntity.GenreEntityBuilder genreEntity = GenreEntity.builder();

        genreEntity.name( request.getName() );

        return genreEntity.build();
    }
}
