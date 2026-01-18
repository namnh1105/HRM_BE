package com.hainam.worksphere.shared.audit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
        @Index(name = "idx_audit_user", columnList = "userId"),
        @Index(name = "idx_audit_field", columnList = "fieldName"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_request", columnList = "requestId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ========= Business context ========= */

    @Column(nullable = false, length = 50)
    private String action;
    // CREATE, UPDATE, DELETE, UPDATE_PROFILE, ASSIGN_ROLE...

    @Column(nullable = false, length = 100)
    private String entityType;
    // USER, EMPLOYEE, ROLE...

    @Column(nullable = false, length = 100)
    private String entityId;

    @Column(length = 100)
    private String fieldName;
    // given_name, is_active, salary...

    /* ========= Value change ========= */

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    /* ========= Actor ========= */

    @Column(length = 50)
    private String userId;

    @Column(length = 100)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    /* ========= Request context ========= */

    @Column(length = 50)
    private String requestId;
    // dùng để group nhiều audit trong 1 request

    @Column(length = 100)
    private String requestMethod;

    @Column(length = 500)
    private String requestUrl;

    /* ========= Status ========= */

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 20)
    private String status;
    // SUCCESS, FAILED

    @Column(length = 1000)
    private String errorMessage;


    /* ========= Lifecycle ========= */

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}
