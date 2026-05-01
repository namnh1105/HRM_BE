package com.hainam.worksphere.user.dto.response;

import com.hainam.worksphere.employee.dto.response.EmployeeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String email;

    private String avatarUrl;

    private EmployeeResponse employee;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID createdBy;

    private UUID updatedBy;

    private Boolean isDeleted;

    private Instant deletedAt;

    private UUID deletedBy;
}
