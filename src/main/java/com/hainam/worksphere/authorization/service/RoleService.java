package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.repository.RoleRepository;
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
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.existsByCode(role.getCode())) {
            throw new IllegalArgumentException("Role with code '" + role.getCode() + "' already exists");
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(UUID roleId, Role roleUpdate) {
        Role existingRole = getRoleById(roleId);
        if (!existingRole.getCode().equals(roleUpdate.getCode()) &&
            roleRepository.existsByCode(roleUpdate.getCode())) {
            throw new IllegalArgumentException("Role with code '" + roleUpdate.getCode() + "' already exists");
        }

        existingRole.setCode(roleUpdate.getCode());
        existingRole.setDisplayName(roleUpdate.getDisplayName());
        existingRole.setDescription(roleUpdate.getDescription());
        existingRole.setIsActive(roleUpdate.getIsActive());

        return roleRepository.save(existingRole);
    }

    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
    }

    public Optional<Role> getRoleByCode(String code) {
        return roleRepository.findByCode(code);
    }

    public Page<Role> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public List<Role> getAllActiveRoles() {
        return roleRepository.findByIsActiveTrue();
    }

    public List<Role> getAllSystemRoles() {
        return roleRepository.findByIsSystemTrue();
    }

    public List<Role> getRolesByUserId(UUID userId) {
        return roleRepository.findByUserId(userId);
    }

    public List<Role> getRolesByPermissionCode(String permissionCode) {
        return roleRepository.findByPermissionCode(permissionCode);
    }

    public List<Role> getRolesByCodes(Set<String> codes) {
        return roleRepository.findByCodeIn(codes);
    }

    public List<Role> searchRoles(String search) {
        return roleRepository.searchByCodeOrDisplayName(search);
    }


    public boolean userHasRole(UUID userId, UUID roleId) {
        return roleRepository.userHasRole(userId, roleId);
    }

    public boolean userHasRole(UUID userId, String roleCode) {
        return roleRepository.userHasRoleByCode(userId, roleCode);
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = getRoleById(roleId);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException("Cannot delete system role: " + role.getCode());
        }

        role.setIsActive(false);
        roleRepository.save(role);
    }

    public boolean existsByCode(String code) {
        return roleRepository.existsByCode(code);
    }
}
