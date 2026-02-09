package com.hainam.worksphere.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayrollRequest {

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("month")
    private Integer month;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("salary_coefficient")
    private Double salaryCoefficient;

    @JsonProperty("working_days")
    private Integer workingDays;

    @JsonProperty("actual_working_days")
    private Integer actualWorkingDays;

    @JsonProperty("overtime_hours")
    private Double overtimeHours;

    @JsonProperty("overtime_pay")
    private Double overtimePay;

    @JsonProperty("allowance")
    private Double allowance;

    @JsonProperty("bonus")
    private Double bonus;

    @JsonProperty("social_insurance")
    private Double socialInsurance;

    @JsonProperty("health_insurance")
    private Double healthInsurance;

    @JsonProperty("unemployment_insurance")
    private Double unemploymentInsurance;

    @JsonProperty("personal_income_tax")
    private Double personalIncomeTax;

    @JsonProperty("note")
    private String note;
}
