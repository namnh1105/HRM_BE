package com.hainam.worksphere.degree.dto.response;

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
public class DegreeResponse {

    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("degree_name")
    private String degreeName;

    @JsonProperty("degree_level")
    private String degreeLevel;

    private String major;

    private String institution;

    @JsonProperty("graduation_date")
    private LocalDate graduationDate;

    private Double gpa;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    private String note;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
