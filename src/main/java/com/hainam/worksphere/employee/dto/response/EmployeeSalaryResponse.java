package com.hainam.worksphere.employee.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

