package com.hainam.worksphere.relative.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private UUID employeeId;

    private String employeeName;

    private String fullName;

    private String relationship;

    private LocalDate dateOfBirth;

    private String phone;

    private String idCardNumber;

    private String occupation;

    private String address;

    private Boolean isEmergencyContact;

    private Boolean isDependent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
