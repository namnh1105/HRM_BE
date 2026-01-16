package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional
    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new IllegalArgumentException("Permission with code '" + permission.getCode() + "' already exists");
        }

        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission updatePermission(UUID permissionId, Permission permissionUpdate) {
        Permission existingPermission = getPermissionById(permissionId);

        if (!existingPermission.getCode().equals(permissionUpdate.getCode()) &&
            permissionRepository.existsByCode(permissionUpdate.getCode())) {
            throw new IllegalArgumentException("Permission with code '" + permissionUpdate.getCode() + "' already exists");
        }

        existingPermission.setCode(permissionUpdate.getCode());
        existingPermission.setDisplayName(permissionUpdate.getDisplayName());
        existingPermission.setDescription(permissionUpdate.getDescription());
        existingPermission.setResource(permissionUpdate.getResource());
        existingPermission.setAction(permissionUpdate.getAction());
        existingPermission.setIsActive(permissionUpdate.getIsActive());

        return permissionRepository.save(existingPermission);
    }

    public Permission getPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
    }

    public Optional<Permission> getPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    public Page<Permission> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findByIsActiveTrue();
    }

    public List<Permission> getAllSystemPermissions() {
        return permissionRepository.findByIsSystemTrue();
    }

    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    public List<Permission> getPermissionsByAction(String action) {
        return permissionRepository.findByAction(action);
    }

    public List<Permission> getPermissionsByResourceAndAction(String resource, String action) {
        return permissionRepository.findByResourceAndAction(resource, action);
    }

    public List<Permission> getPermissionsByRoleId(UUID roleId) {
        return permissionRepository.findByRoleId(roleId);
    }

    public List<Permission> getPermissionsByRoleCode(String roleCode) {
        return permissionRepository.findByRoleCode(roleCode);
    }

    public List<Permission> getPermissionsByUserId(UUID userId) {
        return permissionRepository.findByUserId(userId);
    }

    public List<Permission> getPermissionsByCodes(Set<String> codes) {
        return permissionRepository.findByCodeIn(codes);
    }

    public List<Permission> searchPermissions(String search) {
        return permissionRepository.searchByCodeOrDisplayName(search);
    }

    @Transactional
    public void deletePermission(UUID permissionId) {
        Permission permission = getPermissionById(permissionId);

        if (permission.getIsSystem()) {
            throw new IllegalArgumentException("Cannot delete system permission: " + permission.getCode());
        }

        permission.setIsActive(false);
        permissionRepository.save(permission);
    }

    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }
}
