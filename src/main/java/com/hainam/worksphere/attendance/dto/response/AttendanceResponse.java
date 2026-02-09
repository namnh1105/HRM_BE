package com.hainam.worksphere.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("work_date")
    private LocalDate workDate;

    @JsonProperty("check_in_time")
    private LocalTime checkInTime;

    @JsonProperty("check_out_time")
    private LocalTime checkOutTime;

    @JsonProperty("check_in_ip")
    private String checkInIp;

    @JsonProperty("check_out_ip")
    private String checkOutIp;

    @JsonProperty("check_in_location")
    private String checkInLocation;

    @JsonProperty("check_out_location")
    private String checkOutLocation;

    private AttendanceStatus status;

    @JsonProperty("working_hours")
    private Double workingHours;

    @JsonProperty("overtime_hours")
    private Double overtimeHours;

    @JsonProperty("late_minutes")
    private Integer lateMinutes;

    @JsonProperty("early_leave_minutes")
    private Integer earlyLeaveMinutes;

    private String note;

    @JsonProperty("work_shift_id")
    private UUID workShiftId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by")
    private UUID createdBy;

    @JsonProperty("updated_by")
    private UUID updatedBy;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("deleted_by")
    private UUID deletedBy;
}
