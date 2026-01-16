package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.RolePermission;
import com.hainam.worksphere.authorization.repository.RolePermissionRepository;
import com.hainam.worksphere.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Transactional
    public RolePermission assignPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleService.getRoleById(roleId);
        Permission permission = permissionService.getPermissionById(permissionId);

        Optional<RolePermission> existing = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
        if (existing.isPresent()) {
            if (existing.get().getIsActive()) {
                throw new IllegalArgumentException("Permission is already assigned to role");
            } else {
                existing.get().setIsActive(true);
                return rolePermissionRepository.save(existing.get());
            }
        }

        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .isActive(true)
                .build();

        return rolePermissionRepository.save(rolePermission);
    }

    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission assignment not found"));

        rolePermission.setIsActive(false);
        rolePermissionRepository.save(rolePermission);
    }

    public Optional<RolePermission> getRolePermission(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
    }

    public List<RolePermission> getRolePermissionsByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public List<RolePermission> getActiveRolePermissionsByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleIdAndIsActiveTrue(roleId);
    }

    public List<RolePermission> getRolePermissionsByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    public List<RolePermission> getActiveRolePermissionsByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermissionIdAndIsActiveTrue(permissionId);
    }

    public boolean roleHasPermission(UUID roleId, UUID permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(roleId, permissionId);
    }

    public boolean roleHasPermission(String roleCode, String permissionCode) {
        return rolePermissionRepository.existsByRoleCodeAndPermissionCodeAndIsActiveTrue(roleCode, permissionCode);
    }

    @Transactional
    public void deactivateAllPermissionsForRole(UUID roleId) {
        rolePermissionRepository.deactivateByRoleId(roleId);
    }

    @Transactional
    public void deactivateAllRolesForPermission(UUID permissionId) {
        log.info("Deactivating all roles for permission {}", permissionId);
        rolePermissionRepository.deactivateByPermissionId(permissionId);
    }

    @Transactional
    public void activateRolePermission(UUID roleId, UUID permissionId) {
        rolePermissionRepository.activate(roleId, permissionId);
    }

    @Transactional
    public void deactivateRolePermission(UUID roleId, UUID permissionId) {
        rolePermissionRepository.deactivate(roleId, permissionId);
    }

    @Transactional
    public void assignPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        for (UUID permissionId : permissionIds) {
            try {
                assignPermissionToRole(roleId, permissionId);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to assign permission {} to role {}: {}", permissionId, roleId, e.getMessage());
            }
        }
    }

    @Transactional
    public void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds) {
        for (UUID permissionId : permissionIds) {
            removePermissionFromRole(roleId, permissionId);
        }
    }
}
