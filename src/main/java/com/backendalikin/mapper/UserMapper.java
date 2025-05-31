package com.backendalikin.mapper;

import com.backendalikin.dto.request.SignupRequest;
import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserBasicResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "followersCount", expression = "java(entity.getFollowers().size())")
    @Mapping(target = "followingCount", expression = "java(entity.getFollowing().size())")
    @Mapping(target = "isFollowing", ignore = true) // Esto se setea en el servicio
    UserResponse toUserResponse(UserEntity entity);

    UserBasicResponse toUserBasicResponse(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "communities", ignore = true)
    @Mapping(target = "playlists", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget UserEntity entity);
}