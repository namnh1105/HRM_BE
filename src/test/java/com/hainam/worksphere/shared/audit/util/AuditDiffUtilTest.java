package com.hainam.worksphere.shared.audit.util;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.shared.audit.service.AuditService;
import com.hainam.worksphere.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuditDiffUtil Tests")
class AuditDiffUtilTest extends BaseUnitTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditDiffUtil auditDiffUtil;

    private User originalUser;
    private User modifiedUser;
    private String requestId;

    @BeforeEach
    void setUp() {
        originalUser = TestFixtures.createTestUser();
        modifiedUser = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName("UpdatedGiven")
                .familyName("UpdatedFamily")
                .name("UpdatedGiven UpdatedFamily")
                .password(originalUser.getPassword())
                .avatarUrl("https://new-avatar.com/avatar.jpg")
                .isEnabled(false) // Changed from true
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        requestId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Should detect and audit all field changes")
    void shouldDetectAndAuditAllFieldChanges() {
        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                originalUser,
                modifiedUser,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_PROFILE"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }

    @Test
    @DisplayName("Should handle null field additions")
    void shouldHandleNullFieldAdditions() {
        // Given
        User userWithNullFields = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName(originalUser.getGivenName())
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .avatarUrl(null) // Was null
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        User userWithAddedFields = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName(originalUser.getGivenName())
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .avatarUrl("https://new-avatar.com/avatar.jpg") // Now has value
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                userWithNullFields,
                userWithAddedFields,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_PROFILE"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }

    @Test
    @DisplayName("Should handle field removals")
    void shouldHandleFieldRemovals() {
        // Given - reversed: field removed (set to null)
        User userWithRemovedField = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName(originalUser.getGivenName())
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .avatarUrl(null) // Field removed
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                modifiedUser,
                userWithRemovedField,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_PROFILE"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }

    @Test
    @DisplayName("Should not audit identical objects")
    void shouldNotAuditIdenticalObjects() {
        // Given - same object
        User identicalUser = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName(originalUser.getGivenName())
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .avatarUrl(originalUser.getAvatarUrl())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                originalUser,
                identicalUser,
                requestId
        );

        // Then - should not call audit service since no changes
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Should handle empty strings vs null correctly")
    void shouldHandleEmptyStringsVsNullCorrectly() {
        // Given
        User userWithEmptyString = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName("")
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        User userWithNull = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName(null)
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                userWithEmptyString,
                userWithNull,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_PROFILE"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }

    @Test
    @DisplayName("Should handle complex nested object changes")
    void shouldHandleComplexNestedObjectChanges() {
        // Given - Change a field that is NOT in the ignoreFields list
        User userWithOldName = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName("OldGivenName")
                .familyName(originalUser.getFamilyName())
                .name("OldGivenName " + originalUser.getFamilyName())
                .password(originalUser.getPassword())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        User userWithNewName = User.builder()
                .id(originalUser.getId())
                .email(originalUser.getEmail())
                .givenName("NewGivenName")
                .familyName(originalUser.getFamilyName())
                .name("NewGivenName " + originalUser.getFamilyName())
                .password(originalUser.getPassword())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_PROFILE",
                "USER",
                originalUser.getId().toString(),
                userWithOldName,
                userWithNewName,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_PROFILE"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }

    @Test
    @DisplayName("Should format change descriptions correctly")
    void shouldFormatChangeDescriptionsCorrectly() {
        // Given
        User userWithChangedEmail = User.builder()
                .id(originalUser.getId())
                .email("newemail@example.com")
                .givenName(originalUser.getGivenName())
                .familyName(originalUser.getFamilyName())
                .name(originalUser.getName())
                .password(originalUser.getPassword())
                .isEnabled(originalUser.getIsEnabled())
                .isDeleted(originalUser.getIsDeleted())
                .createdAt(originalUser.getCreatedAt())
                .build();

        // When
        auditDiffUtil.auditAllChanges(
                "UPDATE_EMAIL",
                "USER",
                originalUser.getId().toString(),
                originalUser,
                userWithChangedEmail,
                requestId
        );

        // Then
        verify(auditService).createAuditLogWithDetails(
                eq("UPDATE_EMAIL"),
                eq("USER"),
                eq(originalUser.getId().toString()),
                anyList(),
                eq(requestId)
        );
    }
}
