package com.hainam.worksphere.contract.dto.response;

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
public class ContractResponse {

    private UUID id;

    @JsonProperty("contract_code")
    private String contractCode;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("contract_type")
    private String contractType;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("signing_date")
    private LocalDate signingDate;

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("salary_coefficient")
    private Double salaryCoefficient;

    private String status;

    private String note;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
