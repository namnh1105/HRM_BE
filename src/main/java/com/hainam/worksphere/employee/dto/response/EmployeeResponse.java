package com.hainam.worksphere.employee.dto.response;

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
public class EmployeeResponse {

    private UUID id;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    private String phone;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @JsonProperty("id_card_number")
    private String idCardNumber;

    @JsonProperty("permanent_address")
    private String permanentAddress;

    @JsonProperty("current_address")
    private String currentAddress;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("department_id")
    private UUID departmentId;

    @JsonProperty("department_name")
    private String departmentName;

    private String position;

    @JsonProperty("join_date")
    private LocalDate joinDate;

    @JsonProperty("leave_date")
    private LocalDate leaveDate;

    @JsonProperty("employment_status")
    private String employmentStatus;

    @JsonProperty("bank_account_number")
    private String bankAccountNumber;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("tax_code")
    private String taxCode;

    @JsonProperty("social_insurance_number")
    private String socialInsuranceNumber;

    @JsonProperty("health_insurance_number")
    private String healthInsuranceNumber;


    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
