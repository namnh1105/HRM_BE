package com.hainam.worksphere.contract.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.contract.domain.ContractType;
import jakarta.validation.constraints.NotBlank;
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
public class CreateContractRequest {

    @NotBlank(message = "Contract code is required")
    @JsonProperty("contract_code")
    private String contractCode;

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private UUID employeeId;

    @NotNull(message = "Contract type is required")
    @JsonProperty("contract_type")
    private ContractType contractType;

    @NotNull(message = "Start date is required")
    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("signing_date")
    private LocalDate signingDate;

    @NotNull(message = "Base salary is required")
    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("salary_coefficient")
    private Double salaryCoefficient;

    private String note;

    @JsonProperty("attachment_url")
    private String attachmentUrl;
}
