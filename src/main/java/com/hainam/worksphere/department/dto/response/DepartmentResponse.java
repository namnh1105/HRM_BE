package com.hainam.worksphere.department.dto.response;

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
public class DepartmentResponse {

    private UUID id;

    private String name;

    private String code;

    private String description;

    private String phone;

    private String email;

    @JsonProperty("manager_id")
    private UUID managerId;

    @JsonProperty("manager_name")
    private String managerName;

    @JsonProperty("parent_department_id")
    private UUID parentDepartmentId;

    @JsonProperty("parent_department_name")
    private String parentDepartmentName;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
