package com.hainam.worksphere.employee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("end_date")
    private LocalDate endDate;
}

