package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.dto.response.UserRoleResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    private final RoleMapper roleMapper;

    public UserRoleMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserRoleResponse toResponse(UserRole entity) {
        if (entity == null) {
            return null;
        }

        return UserRoleResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .role(roleMapper.toSimpleResponse(entity.getRole()))
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
