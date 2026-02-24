package com.hainam.worksphere.attendance.controller;

import com.hainam.worksphere.attendance.dto.request.CheckInRequest;
import com.hainam.worksphere.attendance.dto.request.CheckOutRequest;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import com.hainam.worksphere.attendance.service.AttendanceService;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.authorization.security.RequirePermission;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.dto.ApiResponse;
import com.hainam.worksphere.shared.exception.EmployeeNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance Management")
@SecurityRequirement(name = "Bearer Authentication")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/check-in")
    @Operation(summary = "Check in for today")
    @RequirePermission(PermissionType.CREATE_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CheckInRequest request,
            HttpServletRequest httpRequest
    ) {
        Employee employee = getEmployeeByUserId(userPrincipal.getId());
        String ipAddress = extractIpAddress(httpRequest);
        AttendanceResponse response = attendanceService.checkIn(employee.getId(), request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully", response));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out for today")
    @RequirePermission(PermissionType.UPDATE_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CheckOutRequest request,
            HttpServletRequest httpRequest
    ) {
        Employee employee = getEmployeeByUserId(userPrincipal.getId());
        String ipAddress = extractIpAddress(httpRequest);
        AttendanceResponse response = attendanceService.checkOut(employee.getId(), request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", response));
    }

    @GetMapping("/me/today")
    @Operation(summary = "Get current employee's today attendance")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Employee employee = getEmployeeByUserId(userPrincipal.getId());
        Optional<AttendanceResponse> response = attendanceService.getTodayAttendance(employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response.orElse(null)));
    }

    @GetMapping("/me/history")
    @Operation(summary = "Get current employee's attendance history")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Employee employee = getEmployeeByUserId(userPrincipal.getId());
        List<AttendanceResponse> response = attendanceService.getAttendanceHistory(employee.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/history")
    @Operation(summary = "Get employee's attendance history (admin)")
    @RequirePermission(PermissionType.VIEW_ATTENDANCE)
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getEmployeeAttendanceHistory(
            @PathVariable UUID employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<AttendanceResponse> response = attendanceService.getAttendanceHistory(employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Employee getEmployeeByUserId(UUID userId) {
        return employeeRepository.findActiveByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byUserId(userId.toString()));
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
