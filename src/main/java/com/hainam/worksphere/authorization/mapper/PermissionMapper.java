package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.dto.request.CreatePermissionRequest;
import com.hainam.worksphere.authorization.dto.request.UpdatePermissionRequest;
import com.hainam.worksphere.authorization.dto.response.PermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {
    public Permission toEntity(CreatePermissionRequest request) {
        if (request == null) {
            return null;
        }

        return Permission.builder()
                .code(request.getCode())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(request.getAction())
                .isActive(request.getIsActive())
                .isSystem(false)
                .build();
    }

    public Permission updateEntity(Permission entity, UpdatePermissionRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        entity.setCode(request.getCode());
        entity.setDisplayName(request.getDisplayName());
        entity.setDescription(request.getDescription());
        entity.setResource(request.getResource());
        entity.setAction(request.getAction());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        return entity;
    }

    public PermissionResponse toResponse(Permission entity) {
        if (entity == null) {
            return null;
        }

        return PermissionResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .resource(entity.getResource())
                .action(entity.getAction())
                .isSystem(entity.getIsSystem())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
