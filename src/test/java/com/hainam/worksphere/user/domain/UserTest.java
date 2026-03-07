package com.hainam.worksphere.user.domain;

import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("User Domain Tests")
class UserTest extends BaseUnitTest {

    @Test
    @DisplayName("Should create user with builder pattern")
    void shouldCreateUserWithBuilderPattern() {
        // Given
        UUID id = UUID.randomUUID();
        String givenName = "John";
        String familyName = "Doe";
        String email = "john.doe@example.com";
        String password = "encodedPassword";
        String name = "John Doe";
        Instant now = Instant.now();

        // When
        User user = User.builder()
                .id(id)
                .givenName(givenName)
                .familyName(familyName)
                .email(email)
                .password(password)
                .name(name)
                .isEnabled(true)
                .isDeleted(false)
                .createdAt(now)
                .build();

        // Then
        assertAll(
                () -> assertThat(user.getId()).isEqualTo(id),
                () -> assertThat(user.getGivenName()).isEqualTo(givenName),
                () -> assertThat(user.getFamilyName()).isEqualTo(familyName),
                () -> assertThat(user.getEmail()).isEqualTo(email),
                () -> assertThat(user.getPassword()).isEqualTo(password),
                () -> assertThat(user.getName()).isEqualTo(name),
                () -> assertThat(user.getIsEnabled()).isTrue(),
                () -> assertThat(user.getIsDeleted()).isFalse(),
                () -> assertThat(user.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should create user with default values")
    void shouldCreateUserWithDefaultValues() {
        // When
        User user = User.builder()
                .email("test@example.com")
                .build();

        // Then
        assertAll(
                () -> assertThat(user.getEmail()).isEqualTo("test@example.com"),
                () -> assertThat(user.getIsEnabled()).isTrue(), // Default value
                () -> assertThat(user.getIsDeleted()).isFalse()  // Default value
        );
    }

    @Test
    @DisplayName("Should create Google user")
    void shouldCreateGoogleUser() {
        // Given
        String googleId = "google123";
        String avatarUrl = "https://lh3.googleusercontent.com/avatar.jpg";

        // When
        User googleUser = User.builder()
                .givenName("Jane")
                .familyName("Smith")
                .email("jane.smith@gmail.com")
                .googleId(googleId)
                .avatarUrl(avatarUrl)
                .isEnabled(true)
                .isDeleted(false)
                .build();

        // Then
        assertAll(
                () -> assertThat(googleUser.getGoogleId()).isEqualTo(googleId),
                () -> assertThat(googleUser.getAvatarUrl()).isEqualTo(avatarUrl),
                () -> assertThat(googleUser.getPassword()).isNull(), // Google users don't have passwords
                () -> assertThat(googleUser.getIsEnabled()).isTrue()
        );
    }

    @Test
    @DisplayName("Should handle user with all constructor")
    void shouldHandleUserWithAllConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        String givenName = "Alice";
        String familyName = "Johnson";
        String email = "alice.johnson@example.com";
        String password = "hashedPassword";
        String name = "Alice Johnson";
        String googleId = null;
        String avatarUrl = null;
        Boolean isEnabled = true;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        Boolean isDeleted = false;
        Instant deletedAt = null;
        UUID deletedBy = null;

        // When
        User user = new User(id, givenName, familyName, email, password, name, googleId, avatarUrl,
                           isEnabled, createdAt, updatedAt, createdBy, updatedBy, isDeleted,
                           deletedAt, deletedBy);

        // Then
        assertAll(
                () -> assertThat(user.getId()).isEqualTo(id),
                () -> assertThat(user.getGivenName()).isEqualTo(givenName),
                () -> assertThat(user.getFamilyName()).isEqualTo(familyName),
                () -> assertThat(user.getEmail()).isEqualTo(email),
                () -> assertThat(user.getPassword()).isEqualTo(password),
                () -> assertThat(user.getName()).isEqualTo(name),
                () -> assertThat(user.getGoogleId()).isNull(),
                () -> assertThat(user.getAvatarUrl()).isNull(),
                () -> assertThat(user.getIsEnabled()).isTrue(),
                () -> assertThat(user.getCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(user.getUpdatedAt()).isEqualTo(updatedAt),
                () -> assertThat(user.getCreatedBy()).isEqualTo(createdBy),
                () -> assertThat(user.getUpdatedBy()).isEqualTo(updatedBy),
                () -> assertThat(user.getIsDeleted()).isFalse(),
                () -> assertThat(user.getDeletedAt()).isNull(),
                () -> assertThat(user.getDeletedBy()).isNull()
        );
    }

    @Test
    @DisplayName("Should create user with no args constructor")
    void shouldCreateUserWithNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertThat(user).isNotNull();
    }

    @Test
    @DisplayName("Should handle soft deletion fields")
    void shouldHandleSoftDeletionFields() {
        // Given
        Instant deletionTime = Instant.now();
        UUID deletedBy = UUID.randomUUID();

        // When
        User user = User.builder()
                .email("deleted@example.com")
                .isDeleted(true)
                .deletedAt(deletionTime)
                .deletedBy(deletedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(user.getIsDeleted()).isTrue(),
                () -> assertThat(user.getDeletedAt()).isEqualTo(deletionTime),
                () -> assertThat(user.getDeletedBy()).isEqualTo(deletedBy)
        );
    }

    @Test
    @DisplayName("Should handle user status changes")
    void shouldHandleUserStatusChanges() {
        // Given
        User user = User.builder()
                .email("status@example.com")
                .isEnabled(true)
                .build();

        // When - Disable user
        user.setIsEnabled(false);

        // Then
        assertThat(user.getIsEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should handle audit timestamps")
    void shouldHandleAuditTimestamps() {
        // Given
        Instant createdTime = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant updatedTime = Instant.now();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();

        // When
        User user = User.builder()
                .email("audit@example.com")
                .createdAt(createdTime)
                .updatedAt(updatedTime)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();

        // Then
        assertAll(
                () -> assertThat(user.getCreatedAt()).isEqualTo(createdTime),
                () -> assertThat(user.getUpdatedAt()).isEqualTo(updatedTime),
                () -> assertThat(user.getCreatedBy()).isEqualTo(createdBy),
                () -> assertThat(user.getUpdatedBy()).isEqualTo(updatedBy)
        );
    }

    @Test
    @DisplayName("Should handle long text values")
    void shouldHandleLongTextValues() {
        // Given
        String longName = "A".repeat(100);
        String longEmail = "a".repeat(80) + "@example.com";

        // When
        User user = User.builder()
                .name(longName)
                .email(longEmail)
                .build();

        // Then
        assertAll(
                () -> assertThat(user.getName()).isEqualTo(longName),
                () -> assertThat(user.getEmail()).isEqualTo(longEmail)
        );
    }

    @Test
    @DisplayName("Should handle user profile updates")
    void shouldHandleUserProfileUpdates() {
        // Given
        User user = User.builder()
                .givenName("Original")
                .familyName("Name")
                .email("original@example.com")
                .build();

        // When
        user.setGivenName("Updated");
        user.setFamilyName("Profile");
        user.setName("Updated Profile");
        user.setAvatarUrl("https://example.com/new-avatar.jpg");

        // Then
        assertAll(
                () -> assertThat(user.getGivenName()).isEqualTo("Updated"),
                () -> assertThat(user.getFamilyName()).isEqualTo("Profile"),
                () -> assertThat(user.getName()).isEqualTo("Updated Profile"),
                () -> assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg")
        );
    }

    @Test
    @DisplayName("Should handle password updates")
    void shouldHandlePasswordUpdates() {
        // Given
        User user = User.builder()
                .email("password@example.com")
                .password("oldPassword")
                .build();

        // When
        user.setPassword("newEncodedPassword");

        // Then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("Should handle Google ID association")
    void shouldHandleGoogleIdAssociation() {
        // Given
        User user = User.builder()
                .email("google@example.com")
                .build();

        // When
        user.setGoogleId("google123456");
        user.setAvatarUrl("https://lh3.googleusercontent.com/photo.jpg");

        // Then
        assertAll(
                () -> assertThat(user.getGoogleId()).isEqualTo("google123456"),
                () -> assertThat(user.getAvatarUrl()).isEqualTo("https://lh3.googleusercontent.com/photo.jpg")
        );
    }
}
