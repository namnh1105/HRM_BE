package com.hainam.worksphere.user.service;

import com.hainam.worksphere.BaseUnitTest;
import com.hainam.worksphere.TestFixtures;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.request.ChangePasswordRequest;
import com.hainam.worksphere.user.dto.request.UpdateProfileRequest;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.user.mapper.UserMapper;
import com.hainam.worksphere.user.mapper.UserUpdateMapper;
import com.hainam.worksphere.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserUpdateMapper userUpdateMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = TestFixtures.createTestUser();
        testUserResponse = UserResponse.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .name(testUser.getName())
                .givenName(testUser.getGivenName())
                .familyName(testUser.getFamilyName())
                .isActive(testUser.getIsEnabled())
                .createdAt(testUser.getCreatedAt())
                .build();

        testUserPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    @DisplayName("Should get current user successfully")
    void shouldGetCurrentUserSuccessfully() {
        // Given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getCurrentUser(testUserPrincipal);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(testUser.getId()),
            () -> assertThat(result.getEmail()).isEqualTo(testUser.getEmail()),
            () -> verify(userRepository).findActiveById(testUser.getId()),
            () -> verify(userMapper).toUserResponse(testUser)
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when current user not found")
    void shouldThrowUserNotFoundExceptionWhenCurrentUserNotFound() {
        // Given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUser(testUserPrincipal))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findActiveById(testUser.getId());
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getUserById(userId);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(userId),
            () -> verify(userRepository).findActiveById(userId),
            () -> verify(userMapper).toUserResponse(testUser)
        );
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findActiveById(nonExistentId);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should get all active users successfully")
    void shouldGetAllActiveUsersSuccessfully() {
        // Given
        List<User> users = Arrays.asList(testUser, TestFixtures.createTestUser("another@test.com"));
        List<UserResponse> userResponses = Arrays.asList(testUserResponse);

        when(userRepository.findAllActive()).thenReturn(users);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        List<UserResponse> result = userService.getAllActiveUsers();

        // Then
        assertAll(
            () -> assertThat(result).hasSize(2),
            () -> verify(userRepository).findAllActive(),
            () -> verify(userMapper, times(2)).toUserResponse(any(User.class))
        );
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfileSuccessfully() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setGivenName("Updated");
        request.setFamilyName("Name");

        User updatedUser = User.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .givenName("Updated")
                .familyName("Name")
                .name("Updated Name")
                .password(testUser.getPassword())
                .isEnabled(testUser.getIsEnabled())
                .isDeleted(testUser.getIsDeleted())
                .createdAt(testUser.getCreatedAt())
                .build();

        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        doNothing().when(userUpdateMapper).updateUserFromRequest(request, testUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.updateProfile(testUserPrincipal, request);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(userRepository).findActiveById(testUser.getId()),
            () -> verify(userUpdateMapper).updateUserFromRequest(request, testUser),
            () -> verify(userRepository).save(any(User.class)),
            () -> verify(userMapper).toUserResponse(updatedUser)
        );
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true); // Use explicit value
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword(testUserPrincipal, request);

        // Then
        verify(userRepository).findActiveById(testUser.getId());
        verify(passwordEncoder).matches("currentPassword", "encodedPassword"); // Use explicit value
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid current password")
    void shouldThrowValidationExceptionForInvalidCurrentPassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false); // Use explicit value

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(testUserPrincipal, request))
                .isInstanceOf(ValidationException.class);

        verify(userRepository).findActiveById(testUser.getId());
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword"); // Use explicit value
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should deactivate account successfully")
    void shouldDeactivateAccountSuccessfully() {
        // Given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateAccount(testUserPrincipal);

        // Then
        verify(userRepository).findActiveById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should soft delete user successfully")
    void shouldSoftDeleteUserSuccessfully() {
        // Given
        UUID userId = testUser.getId();
        UUID deletedBy = UUID.randomUUID();

        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.softDeleteUser(userId, deletedBy);

        // Then
        verify(userRepository).findActiveById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should soft delete current user successfully")
    void shouldSoftDeleteCurrentUserSuccessfully() {
        // Given
        when(userRepository.findActiveById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.softDeleteCurrentUser(testUserPrincipal);

        // Then
        verify(userRepository).findActiveById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should restore user successfully")
    void shouldRestoreUserSuccessfully() {
        // Given
        UUID userId = testUser.getId();
        UUID restoredBy = UUID.randomUUID();
        testUser.setIsDeleted(true);
        testUser.setDeletedAt(LocalDateTime.now());

        User restoredUser = User.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .givenName(testUser.getGivenName())
                .familyName(testUser.getFamilyName())
                .name(testUser.getName())
                .password(testUser.getPassword())
                .isEnabled(testUser.getIsEnabled())
                .isDeleted(false)
                .deletedAt(null)
                .deletedBy(null)
                .createdAt(testUser.getCreatedAt())
                .build();

        when(userRepository.findDeletedById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(restoredUser);
        when(userMapper.toUserResponse(restoredUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.restoreUser(userId, restoredBy);

        // Then
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> verify(userRepository).findDeletedById(userId),
            () -> verify(userRepository).save(any(User.class)),
            () -> verify(userMapper).toUserResponse(restoredUser)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when trying to restore non-deleted user")
    void shouldThrowValidationExceptionWhenTryingToRestoreNonDeletedUser() {
        // Given
        UUID userId = testUser.getId();
        UUID restoredBy = UUID.randomUUID();

        when(userRepository.findDeletedById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.restoreUser(userId, restoredBy))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User not found or not deleted");

        verify(userRepository).findDeletedById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should permanently delete user successfully")
    void shouldPermanentlyDeleteUserSuccessfully() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.permanentDeleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when trying to permanently delete non-existent user")
    void shouldThrowUserNotFoundExceptionWhenTryingToPermanentlyDeleteNonExistentUser() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.permanentDeleteUser(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
