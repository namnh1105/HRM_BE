package com.hainam.worksphere.user.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.user.dto.request.ChangePasswordRequest;
import com.hainam.worksphere.user.dto.request.UpdateProfileRequest;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all active users")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllActiveUsers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @RequirePermission(PermissionType.VIEW_PROFILE)
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @RequirePermission(PermissionType.VIEW_USER)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId
    ) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate account")
    @RequirePermission(PermissionType.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.deactivateAccount(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", null));
    }

    // Soft Delete APIs
    @DeleteMapping("/me")
    @Operation(summary = "Soft delete current user account")
    @RequirePermission(PermissionType.DELETE_PROFILE)
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.softDeleteCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Soft delete user by ID")
    @RequirePermission(PermissionType.DELETE_USER)
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.softDeleteUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PostMapping("/{userId}/restore")
    @Operation(summary = "Restore soft deleted user")
    @RequirePermission(PermissionType.RESTORE_USER)
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UserResponse response = userService.restoreUser(userId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User restored successfully", response));
    }

    @DeleteMapping("/{userId}/permanent")
    @Operation(summary = "Permanently delete user")
    @RequirePermission(PermissionType.PERMANENT_DELETE_USER)
    public ResponseEntity<ApiResponse<Void>> permanentDeleteUser(
            @PathVariable UUID userId
    ) {
        userService.permanentDeleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User permanently deleted", null));
    }
}
