package com.hainam.worksphere.attendance.dto.response;

import com.hainam.worksphere.attendance.domain.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private LocalDate workDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String checkInIp;

    private String checkOutIp;

    private AttendanceStatus status;

    private Double workingHours;

    private Double overtimeHours;

    private Integer lateMinutes;

    private Integer earlyLeaveMinutes;

    private String note;

    private UUID workShiftId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UUID createdBy;

    private UUID updatedBy;

    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    private UUID deletedBy;
}
