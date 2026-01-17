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
    @Mapping(target = "name", source = "name")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "isActive", source = "isEnabled")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "isDeleted", source = "isDeleted")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    UserResponse toUserResponse(User user);
}
