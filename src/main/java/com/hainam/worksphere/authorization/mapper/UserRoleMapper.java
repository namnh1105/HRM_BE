package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.dto.response.UserRoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = RoleMapper.class)
public interface UserRoleMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "toSimpleResponse")
    UserRoleResponse toResponse(UserRole entity);
}
