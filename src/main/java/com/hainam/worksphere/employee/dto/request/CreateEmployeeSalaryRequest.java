package com.hainam.worksphere.employee.dto.request;

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
    private UUID employeeId;

    @NotNull(message = "Base salary is required")
    private Double baseSalary;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private LocalDate endDate;
}
