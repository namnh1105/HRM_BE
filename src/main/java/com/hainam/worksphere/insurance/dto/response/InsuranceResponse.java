package com.hainam.worksphere.insurance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceResponse {

    private UUID id;

    private String code;

    private String name;

    @JsonProperty("insurance_type")
    private String insuranceType;

    private String provider;

    @JsonProperty("employee_rate")
    private Double employeeRate;

    @JsonProperty("employer_rate")
    private Double employerRate;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
