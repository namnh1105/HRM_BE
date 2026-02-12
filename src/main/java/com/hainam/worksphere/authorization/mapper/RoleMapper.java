package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.dto.request.CreateRoleRequest;
import com.hainam.worksphere.authorization.dto.request.UpdateRoleRequest;
import com.hainam.worksphere.authorization.dto.response.RoleResponse;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PermissionMapper.class)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", constant = "false")
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Role entity, UpdateRoleRequest request);

    @Mapping(target = "permissions", ignore = true)
    RoleResponse toResponse(Role entity);

    @AfterMapping
    default void mapPermissions(@MappingTarget RoleResponse response, Role entity) {
        if (entity.getRolePermissions() != null) {
            Set<com.hainam.worksphere.authorization.dto.response.PermissionResponse> permissions = entity.getRolePermissions()
                .stream()
                .filter(rp -> rp.getIsActive())
                .map(rp -> {
                    // MapStruct will auto-inject the PermissionMapper instance
                    return com.hainam.worksphere.authorization.dto.response.PermissionResponse.builder()
                        .id(rp.getPermission().getId())
                        .code(rp.getPermission().getCode())
                        .displayName(rp.getPermission().getDisplayName())
                        .description(rp.getPermission().getDescription())
                        .resource(rp.getPermission().getResource())
                        .action(rp.getPermission().getAction())
                        .isSystem(rp.getPermission().getIsSystem())
                        .isActive(rp.getPermission().getIsActive())
                        .createdAt(rp.getPermission().getCreatedAt())
                        .updatedAt(rp.getPermission().getUpdatedAt())
                        .build();
                })
                .collect(Collectors.toSet());
            response.setPermissions(permissions);
        } else {
            response.setPermissions(Set.of());
        }
    }

    @Named("toSimpleResponse")
    @Mapping(target = "permissions", ignore = true)
    RoleResponse toSimpleResponse(Role entity);
}
