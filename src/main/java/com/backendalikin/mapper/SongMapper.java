package com.backendalikin.mapper;

import com.backendalikin.dto.request.SongRequest;
import com.backendalikin.dto.response.SongBasicResponse;
import com.backendalikin.dto.response.SongResponse;
import com.backendalikin.entity.SongEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, GenreMapper.class})
public interface SongMapper {

    @Mapping(target = "uploader", source = "uploader")
    @Mapping(target = "genres", source = "genres")
    SongResponse toSongResponse(SongEntity entity);

    SongBasicResponse toSongBasicResponse(SongEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "url", ignore = true) // Esto se setea en el servicio
    @Mapping(target = "coverImageUrl", ignore = true) // Esto se setea en el servicio
    @Mapping(target = "duration", ignore = true) // Esto se calcula en el servicio
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    @Mapping(target = "genres", ignore = true) // Los géneros se manejan por separado
    @Mapping(target = "playlists", ignore = true)
    SongEntity toEntity(SongRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "playlists", ignore = true)
    void updateSongFromRequest(SongRequest request, @MappingTarget SongEntity entity);
}