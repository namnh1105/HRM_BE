package com.hainam.worksphere.relative.dto.response;

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
public class RelativeResponse {

    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("full_name")
    private String fullName;

    private String relationship;

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

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
