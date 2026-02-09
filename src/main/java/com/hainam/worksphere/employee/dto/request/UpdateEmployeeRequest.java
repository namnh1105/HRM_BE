package com.hainam.worksphere.employee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class UpdateEmployeeRequest {

    @Size(max = 50)
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 50)
    @JsonProperty("last_name")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @JsonProperty("id_card_number")
    @Size(max = 20)
    private String idCardNumber;

    @JsonProperty("id_card_issued_date")
    private LocalDate idCardIssuedDate;

    @JsonProperty("id_card_issued_place")
    @Size(max = 100)
    private String idCardIssuedPlace;

    @JsonProperty("permanent_address")
    private String permanentAddress;

    @JsonProperty("current_address")
    private String currentAddress;

    @JsonProperty("department_id")
    private UUID departmentId;

    @Size(max = 100)
    private String position;

    @JsonProperty("employment_status")
    private String employmentStatus;

    @JsonProperty("bank_account_number")
    @Size(max = 30)
    private String bankAccountNumber;

    @JsonProperty("bank_name")
    @Size(max = 100)
    private String bankName;

    @JsonProperty("tax_code")
    @Size(max = 20)
    private String taxCode;

    @JsonProperty("social_insurance_number")
    @Size(max = 20)
    private String socialInsuranceNumber;

    @JsonProperty("health_insurance_number")
    @Size(max = 20)
    private String healthInsuranceNumber;

    @JsonProperty("base_salary")
    private Double baseSalary;

    @JsonProperty("leave_date")
    private LocalDate leaveDate;
}
