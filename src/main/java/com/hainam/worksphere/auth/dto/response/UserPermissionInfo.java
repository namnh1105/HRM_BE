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
public class UserPermissionInfo {

    private UUID id;

    private String code;

    @JsonProperty("display_name")
    private String displayName;

    private String description;

    private String resource;

    private String action;

    @JsonProperty("is_active")
    private Boolean isActive;
}
