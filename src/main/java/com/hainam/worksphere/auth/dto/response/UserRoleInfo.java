package com.hainam.worksphere.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleInfo {

    private UUID id;

    private String code;

    @JsonProperty("display_name")
    private String displayName;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;
}
