package com.hainam.worksphere.user.service;

import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.shared.audit.util.AuditDiffUtil;
import com.hainam.worksphere.shared.audit.util.RequestContextUtil;
import com.hainam.worksphere.shared.config.CacheConfig;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.shared.exception.ValidationException;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.request.ChangePasswordRequest;
import com.hainam.worksphere.user.dto.request.UpdateProfileRequest;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.user.mapper.UserMapper;
import com.hainam.worksphere.user.mapper.UserUpdateMapper;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserUpdateMapper userUpdateMapper;
    private final AuditDiffUtil auditDiffUtil;

    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        return userMapper.toUserResponse(user);
    }

    @Cacheable(value = CacheConfig.USER_CACHE, key = "#userId.toString()")
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()"),
        @CacheEvict(value = CacheConfig.USER_BY_EMAIL_CACHE, allEntries = true)
    })
    public UserResponse updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
        User userBefore = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        User originalUser = createUserCopy(userBefore);

        userUpdateMapper.updateUserFromRequest(request, userBefore);
        userBefore.setUpdatedBy(userPrincipal.getId());
        User userAfter = userRepository.save(userBefore);

        String requestId = RequestContextUtil.getRequestId();
        auditDiffUtil.auditAllChanges(
            "UPDATE_PROFILE",
            "USER",
            userAfter.getId().toString(),
            originalUser,
            userAfter,
            requestId
        );

        return userMapper.toUserResponse(userAfter);
    }

    /**
     * Create a copy of user entity for audit comparison
     */
    private User createUserCopy(User original) {
        return User.builder()
                .id(original.getId())
                .email(original.getEmail())
                .name(original.getName())
                .givenName(original.getGivenName())
                .familyName(original.getFamilyName())
                .avatarUrl(original.getAvatarUrl())
                .googleId(original.getGoogleId())
                .isEnabled(original.getIsEnabled())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .createdBy(original.getCreatedBy())
                .updatedBy(original.getUpdatedBy())
                .isDeleted(original.getIsDeleted())
                .deletedAt(original.getDeletedAt())
                .deletedBy(original.getDeletedBy())
                .password(original.getPassword())
                .build();
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()")
    public void changePassword(UserPrincipal userPrincipal, ChangePasswordRequest request) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw ValidationException.passwordMismatch();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(userPrincipal.getId());
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userPrincipal.id.toString()")
    public void deactivateAccount(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        user.setIsEnabled(false);
        user.setUpdatedBy(userPrincipal.getId());
        userRepository.save(user);
    }

    // Soft Delete Methods
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public void softDeleteUser(UUID userId, UUID deletedBy) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);
        user.setUpdatedBy(deletedBy);

        userRepository.save(user);
    }

    @Transactional
    public void softDeleteCurrentUser(UserPrincipal userPrincipal) {
        softDeleteUser(userPrincipal.getId(), userPrincipal.getId());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_ROLES_CACHE, key = "#userId.toString()"),
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString()")
    })
    public UserResponse restoreUser(UUID userId, UUID restoredBy) {
        User user = userRepository.findDeletedById(userId)
                .orElseThrow(() -> new ValidationException("User not found or not deleted"));

        user.setIsDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setUpdatedBy(restoredBy);

        User restoredUser = userRepository.save(user);
        return userMapper.toUserResponse(restoredUser);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE, key = "#userId.toString()")
    public void permanentDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));

        userRepository.delete(user);
    }
}
