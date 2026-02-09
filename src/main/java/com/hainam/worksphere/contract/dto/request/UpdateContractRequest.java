package com.hainam.worksphere.contract.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.contract.domain.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContractRequest {

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("salary_coefficient")
    private Double salaryCoefficient;

    private ContractStatus status;

    private String note;

    @JsonProperty("attachment_url")
    private String attachmentUrl;
}
