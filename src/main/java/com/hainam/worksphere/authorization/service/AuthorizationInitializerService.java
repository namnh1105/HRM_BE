package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.domain.RolePermission;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.authorization.repository.RoleRepository;
import com.hainam.worksphere.authorization.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AuthorizationInitializerService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultPermissions();
        createDefaultRoles();
        assignPermissionsToRoles();
    }

    private void createDefaultPermissions() {
        log.info("Creating default permissions...");

        List<PermissionData> permissions = Arrays.asList(
            new PermissionData("MANAGE_USER", "Manage User", "Create, update, delete users", "USER", "MANAGE"),
            new PermissionData("MANAGE_ROLE", "Manage Role", "Create, update, delete roles", "ROLE", "MANAGE"),
            new PermissionData("MANAGE_PERMISSION", "Manage Permission", "Create, update, delete permissions", "PERMISSION", "MANAGE"),
            new PermissionData("VIEW_PROFILE", "View Profile", "View own profile", "PROFILE", "VIEW"),
            new PermissionData("UPDATE_PROFILE", "Update Profile", "Update own profile", "PROFILE", "UPDATE")
        );

        for (PermissionData permData : permissions) {
            if (!permissionRepository.existsByCode(permData.code)) {
                Permission permission = Permission.builder()
                        .code(permData.code)
                        .displayName(permData.displayName)
                        .description(permData.description)
                        .resource(permData.resource)
                        .action(permData.action)
                        .isSystem(true)
                        .isActive(true)
                        .build();

                permissionRepository.save(permission);
            }
        }
    }

    private void createDefaultRoles() {
        log.info("Creating default roles...");

        List<RoleData> roles = Arrays.asList(
            new RoleData("SUPER_ADMIN", "Super Administrator", "Full system access with all permissions"),
            new RoleData("ADMIN", "Administrator", "Administrative access with user and role management"),
            new RoleData("USER", "User", "Basic user with profile management permissions")
        );

        for (RoleData roleData : roles) {
            if (!roleRepository.existsByCode(roleData.code)) {
                Role role = Role.builder()
                        .code(roleData.code)
                        .displayName(roleData.displayName)
                        .description(roleData.description)
                        .isSystem(true)
                        .isActive(true)
                        .build();

                roleRepository.save(role);
            }
        }
    }

    private void assignPermissionsToRoles() {
        log.info("Assigning permissions to roles...");

        // SUPER_ADMIN gets ALL permissions
        Role superAdmin = roleRepository.findByCode("SUPER_ADMIN").orElse(null);
        if (superAdmin != null) {
            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                if (!rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(superAdmin.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(superAdmin)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to SUPER_ADMIN", permission.getCode());
                }
            }
        }

        // ADMIN gets MANAGE_USER, MANAGE_ROLE permissions
        Role admin = roleRepository.findByCode("ADMIN").orElse(null);
        if (admin != null) {
            String[] adminPermissions = {"MANAGE_USER", "MANAGE_ROLE"};
            for (String permissionCode : adminPermissions) {
                Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
                if (permission != null && !rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(admin.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(admin)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to ADMIN", permission.getCode());
                }
            }
        }

        // USER gets VIEW_PROFILE, UPDATE_PROFILE permissions
        Role user = roleRepository.findByCode("USER").orElse(null);
        if (user != null) {
            String[] userPermissions = {"VIEW_PROFILE", "UPDATE_PROFILE"};
            for (String permissionCode : userPermissions) {
                Permission permission = permissionRepository.findByCode(permissionCode).orElse(null);
                if (permission != null && !rolePermissionRepository.existsByRoleIdAndPermissionIdAndIsActiveTrue(user.getId(), permission.getId())) {
                    RolePermission rolePermission = RolePermission.builder()
                            .role(user)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(rolePermission);
                    log.debug("Assigned permission {} to USER", permission.getCode());
                }
            }
        }

    }

    private static class PermissionData {
        final String code;
        final String displayName;
        final String description;
        final String resource;
        final String action;

        PermissionData(String code, String displayName, String description, String resource, String action) {
            this.code = code;
            this.displayName = displayName;
            this.description = description;
            this.resource = resource;
            this.action = action;
        }
    }

    private static class RoleData {
        final String code;
        final String displayName;
        final String description;

        RoleData(String code, String displayName, String description) {
            this.code = code;
            this.displayName = displayName;
            this.description = description;
        }
    }
}
