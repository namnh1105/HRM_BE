package com.hainam.worksphere.relative.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.relative.domain.RelationshipType;
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
public class CreateRelativeRequest {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employee_id")
    private UUID employeeId;

    @NotBlank(message = "Full name is required")
    @JsonProperty("full_name")
    private String fullName;

    @NotNull(message = "Relationship type is required")
    private RelationshipType relationship;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private String phone;

    @JsonProperty("id_card_number")
    private String idCardNumber;

    private String occupation;

    private String address;

    @JsonProperty("is_emergency_contact")
    private Boolean isEmergencyContact;

    @JsonProperty("is_dependent")
    private Boolean isDependent;
}
