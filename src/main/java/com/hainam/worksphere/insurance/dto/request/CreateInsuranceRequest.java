package com.hainam.worksphere.insurance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.insurance.domain.InsuranceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInsuranceRequest {

    @NotBlank(message = "Insurance code is required")
    private String code;

    @NotBlank(message = "Insurance name is required")
    private String name;

    @NotNull(message = "Insurance type is required")
    @JsonProperty("insurance_type")
    private InsuranceType insuranceType;

    private String provider;

    @JsonProperty("employee_rate")
    private Double employeeRate;

    @JsonProperty("employer_rate")
    private Double employerRate;

    private String description;
}
