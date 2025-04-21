package com.backendalikin.mapper;

import com.backendalikin.dto.request.CommunityRequest;
import com.backendalikin.dto.response.CommunityBasicResponse;
import com.backendalikin.dto.response.CommunityResponse;
import com.backendalikin.entity.CommunityEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PlaylistMapper.class})
public interface CommunityMapper {

    @Mapping(target = "leader", source = "leader")
    @Mapping(target = "membersCount", expression = "java(entity.getMembers().size())")
    @Mapping(target = "isMember", ignore = true) // Esto se setea en el servicio
    @Mapping(target = "userRole", ignore = true) // Esto se setea en el servicio
    @Mapping(target = "radioPlaylist", source = "radioPlaylist")
    CommunityResponse toCommunityResponse(CommunityEntity entity);

    CommunityBasicResponse toCommunityBasicResponse(CommunityEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leader", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "radioPlaylist", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CommunityEntity toEntity(CommunityRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leader", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "radioPlaylist", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", ignore = true) // No permitimos cambiar el nombre
    void updateCommunityFromRequest(CommunityRequest request, @MappingTarget CommunityEntity entity);
}