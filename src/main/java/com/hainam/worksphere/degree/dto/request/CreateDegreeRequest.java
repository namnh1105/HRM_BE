package com.hainam.worksphere.degree.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.degree.domain.DegreeLevel;
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
public class CreateDegreeRequest {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private UUID employeeId;

    @NotBlank(message = "Degree name is required")
    @JsonProperty("degree_name")
    private String degreeName;

    @JsonProperty("degree_level")
    private DegreeLevel degreeLevel;

    private String major;

    private String institution;

    @JsonProperty("graduation_date")
    private LocalDate graduationDate;

    private Double gpa;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    private String note;
}
