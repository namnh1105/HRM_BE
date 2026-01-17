package com.hainam.worksphere.authorization.controller;

import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.dto.request.AssignPermissionsToRoleRequest;
import com.hainam.worksphere.authorization.dto.request.CreateRoleRequest;
import com.hainam.worksphere.authorization.dto.request.UpdateRoleRequest;
import com.hainam.worksphere.authorization.dto.response.RoleResponse;
import com.hainam.worksphere.authorization.mapper.RoleMapper;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.authorization.service.RolePermissionService;
import com.hainam.worksphere.authorization.service.RoleService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.dto.PaginatedApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final RoleMapper roleMapper;

    @PostMapping
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating role: {}", request.getCode());

        Role role = roleMapper.toEntity(request);
        Role createdRole = roleService.createRole(role);
        RoleResponse response = roleMapper.toResponse(createdRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    @GetMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID roleId) {
        log.info("Fetching role with ID: {}", roleId);

        Role role = roleService.getRoleById(roleId);
        RoleResponse response = roleMapper.toResponse(role);

        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<PaginatedApiResponse<RoleResponse>> getAllRoles(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all roles");

        Page<Role> roles = roleService.getAllRoles(pageable);
        Page<RoleResponse> response = roles.map(roleMapper::toSimpleResponse);

        return ResponseEntity.ok(PaginatedApiResponse.success("Roles retrieved successfully", response));
    }

    @GetMapping("/active")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
        log.info("Fetching active roles");

        List<Role> roles = roleService.getAllActiveRoles();
        List<RoleResponse> response = roles.stream()
                .map(roleMapper::toSimpleResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Active roles retrieved successfully", response));
    }

    @PutMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        Role existingRole = roleService.getRoleById(roleId);
        Role updatedRole = roleMapper.updateEntity(existingRole, request);
        Role savedRole = roleService.updateRole(roleId, updatedRole);
        RoleResponse response = roleMapper.toResponse(savedRole);

        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/{roleId}")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID roleId) {
        log.info("Deleting role with ID: {}", roleId);

        roleService.deleteRole(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    @PostMapping("/{roleId}/activate")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> activateRole(@PathVariable UUID roleId) {
        log.info("Activating role with ID: {}", roleId);

        roleService.activateRole(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role activated successfully", null));
    }

    @PostMapping("/{roleId}/deactivate")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> deactivateRole(@PathVariable UUID roleId) {
        log.info("Deactivating role with ID: {}", roleId);

        roleService.deactivateRole(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role deactivated successfully", null));
    }

    @PostMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> assignPermissionsToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        log.info("Assigning {} permissions to role {}", request.getPermissionIds().size(), roleId);

        rolePermissionService.assignPermissionsToRole(roleId, request.getPermissionIds());

        return ResponseEntity.ok(ApiResponse.success("Permissions assigned to role successfully", null));
    }

    @DeleteMapping("/{roleId}/permissions")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<Void>> removePermissionsFromRole(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds) {
        log.info("Removing {} permissions from role {}", permissionIds.size(), roleId);

        rolePermissionService.removePermissionsFromRole(roleId, permissionIds);

        return ResponseEntity.ok(ApiResponse.success("Permissions removed from role successfully", null));
    }

    @GetMapping("/search")
    @RequirePermission(PermissionType.MANAGE_ROLES)
    public ResponseEntity<ApiResponse<List<RoleResponse>>> searchRoles(@RequestParam String query) {
        log.info("Searching roles with query: {}", query);

        List<Role> roles = roleService.searchRoles(query);
        List<RoleResponse> response = roles.stream()
                .map(roleMapper::toSimpleResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Roles search completed", response));
    }
}
