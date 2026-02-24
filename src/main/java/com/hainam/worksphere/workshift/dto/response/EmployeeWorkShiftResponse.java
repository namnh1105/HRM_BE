package com.hainam.worksphere.workshift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private UUID workShiftId;

    private String workShiftName;

    private String workShiftCode;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    private LocalDate date;

    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
