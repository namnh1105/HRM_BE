package com.hainam.worksphere.auth.mapper;

import com.hainam.worksphere.auth.dto.request.RegisterRequest;
import com.hainam.worksphere.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "googleId", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(RegisterRequest registerRequest);
}
