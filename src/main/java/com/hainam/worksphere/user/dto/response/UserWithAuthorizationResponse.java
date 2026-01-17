package com.hainam.worksphere.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.auth.dto.response.UserPermissionInfo;
import com.hainam.worksphere.auth.dto.response.UserRoleInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserWithAuthorizationResponse {

    private UUID id;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String email;

    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private List<UserRoleInfo> roles;

    private List<UserPermissionInfo> permissions;
}
