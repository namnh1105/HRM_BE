package com.hainam.worksphere.employee.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeSalaryRequest {

    private Double baseSalary;

    private LocalDate effectiveDate;

    private LocalDate endDate;
}
