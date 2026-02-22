package com.hainam.worksphere.workshift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkShiftResponse {

    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("work_shift_id")
    private UUID workShiftId;

    @JsonProperty("work_shift_name")
    private String workShiftName;

    @JsonProperty("work_shift_code")
    private String workShiftCode;

    @JsonProperty("shift_start_time")
    private LocalTime shiftStartTime;

    @JsonProperty("shift_end_time")
    private LocalTime shiftEndTime;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
