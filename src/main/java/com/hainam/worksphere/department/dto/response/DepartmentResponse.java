package com.hainam.worksphere.department.dto.response;

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
public class DepartmentResponse {

    private UUID id;

    private String name;

    private String code;

    private String description;

    private String phone;

    private String email;

    private UUID managerId;

    private String managerName;

    private UUID parentDepartmentId;

    private String parentDepartmentName;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
