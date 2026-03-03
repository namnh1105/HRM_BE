package com.hainam.worksphere.employee.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.request.CreateEmployeeRequest;
import com.hainam.worksphere.employee.dto.request.UpdateEmployeeRequest;
import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import com.hainam.worksphere.employee.service.EmployeeService;
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
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all active employees")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE)
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> response = employeeService.getAllActiveEmployees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get employee by ID")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE)
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @PathVariable UUID employeeId
    ) {
        EmployeeResponse response = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current employee profile by logged-in user")
    @RequirePermission(PermissionType.VIEW_PROFILE)
    public ResponseEntity<ApiResponse<EmployeeResponse>> getCurrentEmployee(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse response = employeeService.getEmployeeByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get employees by department")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE)
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByDepartment(
            @PathVariable UUID departmentId
    ) {
        List<EmployeeResponse> response = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get employees by store")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE)
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByStore(
            @PathVariable UUID storeId
    ) {
        List<EmployeeResponse> response = employeeService.getEmployeesByStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    @RequirePermission(PermissionType.CREATE_EMPLOYEE)
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse response = employeeService.createEmployee(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{employeeId}")
    @Operation(summary = "Update employee")
    @RequirePermission(PermissionType.UPDATE_EMPLOYEE)
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeResponse response = employeeService.updateEmployee(employeeId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @DeleteMapping("/{employeeId}")
    @Operation(summary = "Soft delete employee")
    @RequirePermission(PermissionType.DELETE_EMPLOYEE)
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable UUID employeeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        employeeService.softDeleteEmployee(employeeId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
