package com.backendalikin.mapper;

import com.backendalikin.dto.request.PlaylistRequest;
import com.backendalikin.dto.response.PlaylistBasicResponse;
import com.backendalikin.dto.response.PlaylistResponse;
import com.backendalikin.entity.PlaylistEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SongMapper.class})
public interface PlaylistMapper {

    @Mapping(target = "owner", source = "owner") //
    @Mapping(target = "songs", source = "songs") //
    @Mapping(target = "isPublic", source = "public") // <-- AÑADIDO: Mapeo explícito para isPublic
    PlaylistResponse toPlaylistResponse(PlaylistEntity entity);

    PlaylistBasicResponse toPlaylistBasicResponse(PlaylistEntity entity); //

    @Mapping(target = "id", ignore = true) //
    @Mapping(target = "owner", ignore = true) //
    @Mapping(target = "community", ignore = true) //
    @Mapping(target = "createdAt", ignore = true) //
    @Mapping(target = "songs", ignore = true) // Los songs se manejan por separado
    PlaylistEntity toEntity(PlaylistRequest request);

    @Mapping(target = "id", ignore = true) //
    @Mapping(target = "owner", ignore = true) //
    @Mapping(target = "community", ignore = true) //
    @Mapping(target = "createdAt", ignore = true) //
    @Mapping(target = "songs", ignore = true) //
    void updatePlaylistFromRequest(PlaylistRequest request, @MappingTarget PlaylistEntity entity);
}