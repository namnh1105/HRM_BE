package com.hainam.worksphere.auth.mapper;

import com.hainam.worksphere.auth.dto.response.UserPermissionInfo;
import com.hainam.worksphere.auth.dto.response.UserRoleInfo;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAuthorizationMapper {

    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "isActive", source = "user.isEnabled")
    UserWithAuthorizationResponse toUserWithAuthorizationResponse(User user, List<Role> roles, List<Permission> permissions);

    UserRoleInfo toUserRoleInfo(Role role);

    List<UserRoleInfo> toUserRoleInfoList(List<Role> roles);

    UserPermissionInfo toUserPermissionInfo(Permission permission);

    List<UserPermissionInfo> toUserPermissionInfoList(List<Permission> permissions);
}
