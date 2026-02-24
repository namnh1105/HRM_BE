package com.hainam.worksphere.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private Double baseSalary;

    private LocalDate effectiveDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
