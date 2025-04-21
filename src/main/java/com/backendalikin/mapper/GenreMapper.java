package com.backendalikin.mapper;

import com.backendalikin.dto.request.GenreRequest;
import com.backendalikin.dto.response.GenreResponse;
import com.backendalikin.entity.GenreEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreResponse toGenreResponse(GenreEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "songs", ignore = true)
    GenreEntity toEntity(GenreRequest request);
}