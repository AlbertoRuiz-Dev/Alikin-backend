package com.backendalikin.mapper;

import com.backendalikin.dto.request.UserUpdateRequest;
import com.backendalikin.dto.response.UserBasicResponse;
import com.backendalikin.dto.response.UserResponse;
import com.backendalikin.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-16T17:16:55+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( entity.getId() );
        userResponse.name( entity.getName() );
        userResponse.lastName( entity.getLastName() );
        userResponse.nickname( entity.getNickname() );
        userResponse.email( entity.getEmail() );
        userResponse.profilePictureUrl( entity.getProfilePictureUrl() );
        userResponse.phoneNumber( entity.getPhoneNumber() );
        userResponse.birthDate( entity.getBirthDate() );
        userResponse.emailVerified( entity.isEmailVerified() );
        if ( entity.getRole() != null ) {
            userResponse.role( entity.getRole().name() );
        }

        userResponse.followersCount( entity.getFollowers().size() );
        userResponse.followingCount( entity.getFollowing().size() );

        return userResponse.build();
    }

    @Override
    public UserBasicResponse toUserBasicResponse(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserBasicResponse.UserBasicResponseBuilder userBasicResponse = UserBasicResponse.builder();

        userBasicResponse.id( entity.getId() );
        userBasicResponse.name( entity.getName() );
        userBasicResponse.nickname( entity.getNickname() );
        userBasicResponse.profilePictureUrl( entity.getProfilePictureUrl() );

        return userBasicResponse.build();
    }

    @Override
    public void updateUserFromRequest(UserUpdateRequest request, UserEntity entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setLastName( request.getLastName() );
        entity.setProfilePictureUrl( request.getProfilePictureUrl() );
        entity.setPhoneNumber( request.getPhoneNumber() );
    }
}
