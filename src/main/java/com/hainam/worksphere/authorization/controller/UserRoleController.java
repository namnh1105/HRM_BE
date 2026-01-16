package com.hainam.worksphere.authorization.controller;

import com.hainam.worksphere.authorization.domain.UserRole;
import com.hainam.worksphere.authorization.dto.request.AssignRolesRequest;
import com.hainam.worksphere.authorization.dto.response.UserRoleResponse;
import com.hainam.worksphere.authorization.mapper.UserRoleMapper;
import com.hainam.worksphere.authorization.service.UserRoleService;
import com.hainam.worksphere.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-roles")
@RequiredArgsConstructor
@Slf4j
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final UserRoleMapper userRoleMapper;

    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('MANAGE_USER_ROLE')")
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(@Valid @RequestBody AssignRolesRequest request) {
        userRoleService.assignRolesToUser(request.getUserId(), request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("Roles assigned to user successfully", null));
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAuthority('MANAGE_USER_ROLE')")
    public ResponseEntity<ApiResponse<Void>> removeRolesFromUser(@Valid @RequestBody AssignRolesRequest request) {
        userRoleService.removeRolesFromUser(request.getUserId(), request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("Roles removed from user successfully", null));
    }

    @PostMapping("/replace")
    @PreAuthorize("hasAuthority('MANAGE_USER_ROLE')")
    public ResponseEntity<ApiResponse<Void>> replaceUserRoles(@Valid @RequestBody AssignRolesRequest request) {
        userRoleService.replaceUserRoles(request.getUserId(), request.getRoleIds());

        return ResponseEntity.ok(ApiResponse.success("User roles replaced successfully", null));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('READ_USER_ROLE')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getUserRoles(@PathVariable UUID userId) {
        List<UserRole> userRoles = userRoleService.getActiveUserRolesByUserId(userId);
        List<UserRoleResponse> response = userRoles.stream()
                .map(userRoleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", response));
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('READ_USER_ROLE')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getUsersByRole(@PathVariable UUID roleId) {
        List<UserRole> userRoles = userRoleService.getActiveUserRolesByRoleId(roleId);
        List<UserRoleResponse> response = userRoles.stream()
                .map(userRoleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Role users retrieved successfully", response));
    }

    @PostMapping("/user/{userId}/role/{roleId}")
    @PreAuthorize("hasAuthority('MANAGE_USER_ROLE')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        userRoleService.assignRoleToUser(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("Role assigned to user successfully", null));
    }

    @DeleteMapping("/user/{userId}/role/{roleId}")
    @PreAuthorize("hasAuthority('MANAGE_USER_ROLE')")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        userRoleService.removeRoleFromUser(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("Role removed from user successfully", null));
    }

    @GetMapping("/user/{userId}/role/{roleId}/check")
    @PreAuthorize("hasAuthority('READ_USER_ROLE')")
    public ResponseEntity<ApiResponse<Boolean>> checkUserHasRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        boolean hasRole = userRoleService.userHasRole(userId, roleId);

        return ResponseEntity.ok(ApiResponse.success("User role check completed", hasRole));
    }
}
