package com.hainam.worksphere.payroll.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.payroll.domain.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayrollRequest {

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

    @JsonProperty("status")
    private PayrollStatus status;

    @JsonProperty("payment_date")
    private LocalDate paymentDate;

    @JsonProperty("note")
    private String note;
}
