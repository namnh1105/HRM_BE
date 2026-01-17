package com.hainam.worksphere.user.service;

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
import lombok.RequiredArgsConstructor;
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

    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        return userMapper.toUserResponse(user);
    }

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
    public UserResponse updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        userUpdateMapper.updateUserFromRequest(request, user);
        user.setUpdatedBy(userPrincipal.getId());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
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
    public void deactivateAccount(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

        user.setIsEnabled(false);
        user.setUpdatedBy(userPrincipal.getId());
        userRepository.save(user);
    }

    // Soft Delete Methods
    @Transactional
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
    public void permanentDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId.toString()));

        userRepository.delete(user);
    }
}
