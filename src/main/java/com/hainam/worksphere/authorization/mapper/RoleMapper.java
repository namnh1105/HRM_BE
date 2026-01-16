package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.dto.request.CreateRoleRequest;
import com.hainam.worksphere.authorization.dto.request.UpdateRoleRequest;
import com.hainam.worksphere.authorization.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public Role toEntity(CreateRoleRequest request) {
        if (request == null) {
            return null;
        }

        return Role.builder()
                .code(request.getCode())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .isSystem(false) // New roles are not system roles by default
                .build();
    }

    public Role updateEntity(Role entity, UpdateRoleRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        entity.setCode(request.getCode());
        entity.setDisplayName(request.getDisplayName());
        entity.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        return entity;
    }

    public RoleResponse toResponse(Role entity) {
        if (entity == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .isActive(entity.getIsActive())
                .permissions(entity.getRolePermissions() != null ?
                    entity.getRolePermissions().stream()
                        .filter(rp -> rp.getIsActive())
                        .map(rp -> permissionMapper.toResponse(rp.getPermission()))
                        .collect(Collectors.toSet()) : Set.of())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RoleResponse toSimpleResponse(Role entity) {
        if (entity == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
