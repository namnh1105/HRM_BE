package com.hainam.worksphere;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.shared.audit.domain.ActionType;
import com.hainam.worksphere.shared.audit.domain.AuditLog;
import com.hainam.worksphere.shared.audit.domain.AuditLogDetail;
import com.hainam.worksphere.shared.audit.domain.AuditStatus;
import com.hainam.worksphere.shared.constant.PermissionType;
import com.hainam.worksphere.shared.domain.EntityType;
import com.hainam.worksphere.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test fixture factory for creating test data objects
 */
public class TestFixtures {

    // User fixtures
    public static User createTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .givenName("John")
                .familyName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .name("John Doe")
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User createTestUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .givenName("John")
                .familyName("Doe")
                .email(email)
                .password("encodedPassword")
                .name("John Doe")
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User createGoogleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .givenName("Jane")
                .familyName("Smith")
                .email("jane.smith@gmail.com")
                .googleId("google123")
                .name("Jane Smith")
                .avatarUrl("https://example.com/avatar.jpg")
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Role fixtures
    public static Role createTestRole() {
        return Role.builder()
                .id(UUID.randomUUID())
                .code("TEST_ROLE")
                .displayName("Test Role")
                .description("A test role")
                .isSystem(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Role createAdminRole() {
        return Role.builder()
                .id(UUID.randomUUID())
                .code("ADMIN")
                .displayName("Administrator")
                .description("System administrator role")
                .isSystem(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Permission fixtures
    public static Permission createTestPermission() {
        return Permission.builder()
                .id(UUID.randomUUID())
                .code("TEST_PERMISSION")
                .displayName("Test Permission")
                .description("A test permission")
                .resource("USER")
                .action("READ")
                .isSystem(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // RefreshToken fixtures
    public static RefreshToken createTestRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("test-refresh-token")
                .user(createTestUser())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RefreshToken createExpiredRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("expired-refresh-token")
                .user(createTestUser())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isRevoked(false)
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();
    }

    public static RefreshToken createRevokedRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("revoked-refresh-token")
                .user(createTestUser())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Audit fixtures
    public static AuditLog createTestAuditLog() {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .actionType(ActionType.CREATE)
                .actionCode("CREATE_USER")
                .entityType(EntityType.USER)
                .entityId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .username("test@example.com")
                .ipAddress("192.168.1.1")
                .userAgent("Test User Agent")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static AuditLogDetail createTestAuditLogDetail() {
        return AuditLogDetail.builder()
                .id(1L)
                .auditLogId(UUID.randomUUID())
                .fieldName("email")
                .oldValue("old@example.com")
                .newValue("new@example.com")
                .build();
    }
}
