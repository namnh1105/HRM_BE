package com.hainam.worksphere.workshift.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignWorkShiftRequest {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private UUID employeeId;

    @NotNull(message = "Work shift ID is required")
    @JsonProperty("work_shift_id")
    private UUID workShiftId;

    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;
}
