package com.hainam.worksphere.employee.controller;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.dto.request.CreateEmployeeSalaryRequest;
import com.hainam.worksphere.employee.dto.request.UpdateEmployeeSalaryRequest;
import com.hainam.worksphere.employee.dto.response.EmployeeSalaryResponse;
import com.hainam.worksphere.employee.service.EmployeeSalaryService;
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
@RequestMapping("/api/v1/employee-salaries")
@RequiredArgsConstructor
@Tag(name = "Employee Salary Management")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeSalaryController {

    private final EmployeeSalaryService employeeSalaryService;

    @GetMapping
    @Operation(summary = "Get all employee salaries")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<List<EmployeeSalaryResponse>>> getAllSalaries() {
        List<EmployeeSalaryResponse> response = employeeSalaryService.getAllSalaries();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee salary by ID")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<EmployeeSalaryResponse>> getSalaryById(@PathVariable UUID id) {
        EmployeeSalaryResponse response = employeeSalaryService.getSalaryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get salary history by employee ID")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<List<EmployeeSalaryResponse>>> getSalariesByEmployeeId(
            @PathVariable UUID employeeId
    ) {
        List<EmployeeSalaryResponse> response = employeeSalaryService.getSalariesByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/current")
    @Operation(summary = "Get current salary by employee ID")
    @RequirePermission(PermissionType.VIEW_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<EmployeeSalaryResponse>> getCurrentSalary(
            @PathVariable UUID employeeId
    ) {
        EmployeeSalaryResponse response = employeeSalaryService.getCurrentSalaryByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new employee salary record")
    @RequirePermission(PermissionType.CREATE_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<EmployeeSalaryResponse>> createSalary(
            @Valid @RequestBody CreateEmployeeSalaryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeSalaryResponse response = employeeSalaryService.createSalary(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee salary created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee salary record")
    @RequirePermission(PermissionType.UPDATE_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<EmployeeSalaryResponse>> updateSalary(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeSalaryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        EmployeeSalaryResponse response = employeeSalaryService.updateSalary(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee salary updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee salary record (soft delete)")
    @RequirePermission(PermissionType.DELETE_EMPLOYEE_SALARY)
    public ResponseEntity<ApiResponse<Void>> deleteSalary(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        employeeSalaryService.deleteSalary(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee salary deleted successfully", null));
    }
}

