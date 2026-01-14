package com.hainam.worksphere.user.mapper;

import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "givenName", source = "givenName")
    @Mapping(target = "familyName", source = "familyName")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserResponse toUserResponse(User user);
}
