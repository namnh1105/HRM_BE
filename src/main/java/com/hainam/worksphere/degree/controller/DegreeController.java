package com.hainam.worksphere.degree.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.degree.dto.request.CreateDegreeRequest;
import com.hainam.worksphere.degree.dto.response.DegreeResponse;
import com.hainam.worksphere.degree.service.DegreeService;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/degrees")
@RequiredArgsConstructor
@Tag(name = "Degree Management")
@SecurityRequirement(name = "Bearer Authentication")
public class DegreeController {

    private final DegreeService degreeService;

    @PostMapping
    @Operation(summary = "Create a new degree")
    @RequirePermission(PermissionType.CREATE_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> createDegree(
            @Valid @RequestBody CreateDegreeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DegreeResponse response = degreeService.createDegree(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Degree created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all degrees")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getAllDegrees() {
        List<DegreeResponse> response = degreeService.getAllDegrees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get degree by ID")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> getDegreeById(
            @PathVariable UUID id
    ) {
        DegreeResponse response = degreeService.getDegreeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get degrees by employee ID")
    @RequirePermission(PermissionType.VIEW_DEGREE)
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<DegreeResponse> response = degreeService.getByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a degree")
    @RequirePermission(PermissionType.UPDATE_DEGREE)
    public ResponseEntity<ApiResponse<DegreeResponse>> updateDegree(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDegreeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DegreeResponse response = degreeService.updateDegree(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Degree updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a degree (soft delete)")
    @RequirePermission(PermissionType.DELETE_DEGREE)
    public ResponseEntity<ApiResponse<Void>> deleteDegree(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        degreeService.deleteDegree(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Degree deleted successfully", null));
    }
}
