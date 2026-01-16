package com.hainam.worksphere.authorization.service;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.repository.PermissionRepository;
import com.hainam.worksphere.authorization.repository.RoleRepository;
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

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultPermissions();
        createDefaultRoles();
    }

    private void createDefaultPermissions() {
        log.info("Creating default permissions...");

        List<PermissionData> permissions = Arrays.asList(
            new PermissionData("user:create", "Create User", "Create new users", "user", "create"),
            new PermissionData("user:read", "Read User", "View user information", "user", "read"),
            new PermissionData("user:update", "Update User", "Update user information", "user", "update"),
            new PermissionData("user:delete", "Delete User", "Delete users", "user", "delete"),
            new PermissionData("user:list", "List Users", "List all users", "user", "list"),

            new PermissionData("role:create", "Create Role", "Create new roles", "role", "create"),
            new PermissionData("role:read", "Read Role", "View role information", "role", "read"),
            new PermissionData("role:update", "Update Role", "Update role information", "role", "update"),
            new PermissionData("role:delete", "Delete Role", "Delete roles", "role", "delete"),
            new PermissionData("role:list", "List Roles", "List all roles", "role", "list"),

            new PermissionData("permission:create", "Create Permission", "Create new permissions", "permission", "create"),
            new PermissionData("permission:read", "Read Permission", "View permission information", "permission", "read"),
            new PermissionData("permission:update", "Update Permission", "Update permission information", "permission", "update"),
            new PermissionData("permission:delete", "Delete Permission", "Delete permissions", "permission", "delete"),
            new PermissionData("permission:list", "List Permissions", "List all permissions", "permission", "list"),

            new PermissionData("user-role:assign", "Assign User Roles", "Assign roles to users", "user-role", "assign"),
            new PermissionData("user-role:remove", "Remove User Roles", "Remove roles from users", "user-role", "remove"),
            new PermissionData("user-role:read", "Read User Roles", "View user role assignments", "user-role", "read"),

            new PermissionData("system:admin", "System Admin", "Full system administration", "system", "admin"),
            new PermissionData("system:config", "System Config", "System configuration", "system", "config"),
            new PermissionData("system:monitoring", "System Monitoring", "System monitoring", "system", "monitoring"),

            new PermissionData("profile:read", "Read Profile", "View own profile", "profile", "read"),
            new PermissionData("profile:update", "Update Profile", "Update own profile", "profile", "update")
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
            new RoleData("ADMIN", "Administrator", "Administrative access with most permissions"),
            new RoleData("USER_MANAGER", "User Manager", "Can manage users and their roles"),
            new RoleData("USER", "User", "Basic user with limited permissions"),
            new RoleData("GUEST", "Guest", "Read-only access to basic features")
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
