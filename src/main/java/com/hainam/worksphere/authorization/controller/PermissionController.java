package com.hainam.worksphere.authorization.controller;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.dto.request.CreatePermissionRequest;
import com.hainam.worksphere.authorization.dto.request.UpdatePermissionRequest;
import com.hainam.worksphere.authorization.dto.response.PermissionResponse;
import com.hainam.worksphere.authorization.mapper.PermissionMapper;
import com.hainam.worksphere.authorization.service.PermissionService;
import com.hainam.worksphere.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'permission:create')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionMapper.toEntity(request);
        Permission createdPermission = permissionService.createPermission(permission);
        PermissionResponse response = permissionMapper.toResponse(createdPermission);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created successfully", response));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasPermission(null, 'permission:read')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable UUID permissionId) {
        Permission permission = permissionService.getPermissionById(permissionId);
        PermissionResponse response = permissionMapper.toResponse(permission);

        return ResponseEntity.ok(ApiResponse.success("Permission retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'permission:list')")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAllPermissions(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Permission> permissions = permissionService.getAllPermissions(pageable);
        Page<PermissionResponse> response = permissions.map(permissionMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasPermission(null, 'permission:list')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissions() {
        List<Permission> permissions = permissionService.getAllActivePermissions();
        List<PermissionResponse> response = permissions.stream()
                .map(permissionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Active permissions retrieved successfully", response));
    }

    @GetMapping("/resource/{resource}")
    @PreAuthorize("hasPermission(null, 'permission:list')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByResource(@PathVariable String resource) {
        List<Permission> permissions = permissionService.getPermissionsByResource(resource);
        List<PermissionResponse> response = permissions.stream()
                .map(permissionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Permissions for resource retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasPermission(null, 'permission:read')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByUserId(@PathVariable UUID userId) {
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        List<PermissionResponse> response = permissions.stream()
                .map(permissionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("User permissions retrieved successfully", response));
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasPermission(null, 'permission:update')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable UUID permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        Permission existingPermission = permissionService.getPermissionById(permissionId);
        Permission updatedPermission = permissionMapper.updateEntity(existingPermission, request);
        Permission savedPermission = permissionService.updatePermission(permissionId, updatedPermission);
        PermissionResponse response = permissionMapper.toResponse(savedPermission);

        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", response));
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasPermission(null, 'permission:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable UUID permissionId) {

        permissionService.deletePermission(permissionId);

        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", null));
    }

    @PostMapping("/{permissionId}/activate")
    @PreAuthorize("hasPermission(null, 'permission:update')")
    public ResponseEntity<ApiResponse<Void>> activatePermission(@PathVariable UUID permissionId) {
        permissionService.activatePermission(permissionId);

        return ResponseEntity.ok(ApiResponse.success("Permission activated successfully", null));
    }

    @PostMapping("/{permissionId}/deactivate")
    @PreAuthorize("hasPermission(null, 'permission:update')")
    public ResponseEntity<ApiResponse<Void>> deactivatePermission(@PathVariable UUID permissionId) {
        permissionService.deactivatePermission(permissionId);

        return ResponseEntity.ok(ApiResponse.success("Permission deactivated successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasPermission(null, 'permission:list')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> searchPermissions(@RequestParam String query) {
        List<Permission> permissions = permissionService.searchPermissions(query);
        List<PermissionResponse> response = permissions.stream()
                .map(permissionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Permissions search completed", response));
    }
}
