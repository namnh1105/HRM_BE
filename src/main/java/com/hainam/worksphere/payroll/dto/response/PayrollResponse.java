package com.hainam.worksphere.payroll.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
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
public class PayrollResponse {

    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_code")
    private String employeeCode;

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

    @JsonProperty("total_deductions")
    private Double totalDeductions;

    @JsonProperty("total_income")
    private Double totalIncome;

    @JsonProperty("net_salary")
    private Double netSalary;

    @JsonProperty("status")
    private PayrollStatus status;

    @JsonProperty("note")
    private String note;

    @JsonProperty("payment_date")
    private LocalDate paymentDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by")
    private UUID createdBy;

    @JsonProperty("updated_by")
    private UUID updatedBy;
}
