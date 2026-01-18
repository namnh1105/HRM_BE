package com.hainam.worksphere.shared.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogDto {
    private Long id;
    private String action;
    private String entityType;
    private String entityId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private String requestMethod;
    private String requestUrl;
    private LocalDateTime timestamp;
    private String status;
    private String errorMessage;
}
