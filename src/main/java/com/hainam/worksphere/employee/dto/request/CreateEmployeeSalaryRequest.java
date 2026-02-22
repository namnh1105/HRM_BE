package com.hainam.worksphere.employee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeSalaryRequest {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private UUID employeeId;

    @NotNull(message = "Base salary is required")
    @JsonProperty("base_salary")
    private Double baseSalary;

    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("end_date")
    private LocalDate endDate;
}

