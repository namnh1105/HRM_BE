package com.hainam.worksphere.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserWithAuthorizationResponse {

    private UUID id;

    private String givenName;

    private String familyName;

    private String email;

    private String name;

    private String avatarUrl;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID createdBy;

    private UUID updatedBy;

    private Boolean isDeleted;

    private Instant deletedAt;

    private UUID deletedBy;

    private List<String> roles;

    private List<String> permissions;
}
