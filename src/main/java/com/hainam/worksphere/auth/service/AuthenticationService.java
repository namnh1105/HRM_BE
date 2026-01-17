package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.dto.request.*;
import com.hainam.worksphere.auth.dto.response.AuthenticationResponse;
import com.hainam.worksphere.auth.dto.response.TokenResponse;
import com.hainam.worksphere.auth.mapper.AuthMapper;
import com.hainam.worksphere.auth.mapper.AuthResponseMapper;
import com.hainam.worksphere.auth.mapper.UserAuthorizationMapper;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.service.AuthorizationService;
import com.hainam.worksphere.shared.exception.EmailAlreadyExistsException;
import com.hainam.worksphere.shared.exception.InvalidCredentialsException;
import com.hainam.worksphere.shared.exception.RefreshTokenException;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import com.hainam.worksphere.user.mapper.UserMapper;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final AuthResponseMapper authResponseMapper;
    private final AuthorizationService authorizationService;
    private final UserAuthorizationMapper userAuthorizationMapper;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsActiveByEmail(request.getEmail())) {
            throw EmailAlreadyExistsException.withEmail(request.getEmail());
        }

        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        savedUser.setCreatedBy(savedUser.getId());
        savedUser = userRepository.save(savedUser);

        List<Role> userRoles = authorizationService.getUserRoles(savedUser.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(savedUser.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser, userRoles, userPermissions);

        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                savedUser, userRoles, userPermissions);

        return authResponseMapper.toAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpiration() / 1000,
                userWithAuth
        );
    }


    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findActiveById(userPrincipal.getId())
                    .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

            List<Role> userRoles = authorizationService.getUserRoles(user.getId());
            List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

            UserPrincipal enhancedUserPrincipal = UserPrincipal.create(user, userRoles, userPermissions);

            String accessToken = jwtUtil.generateAccessToken(enhancedUserPrincipal);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                    user, userRoles, userPermissions);

            return authResponseMapper.toAuthenticationResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpiration() / 1000,
                    userWithAuth
            );
        } catch (BadCredentialsException e) {
            throw InvalidCredentialsException.create();
        }
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    User activeUser = userRepository.findActiveById(user.getId())
                            .orElseThrow(() -> new RefreshTokenException("User has been deactivated or deleted"));

                    List<Role> userRoles = authorizationService.getUserRoles(activeUser.getId());
                    List<Permission> userPermissions = authorizationService.getUserPermissions(activeUser.getId());

                    UserPrincipal userPrincipal = UserPrincipal.create(activeUser, userRoles, userPermissions);
                    String accessToken = jwtUtil.generateAccessToken(userPrincipal);
                    return authResponseMapper.toTokenResponse(
                            accessToken,
                            jwtUtil.getAccessTokenExpiration() / 1000
                    );
                })
                .orElseThrow(RefreshTokenException::notFound);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .ifPresent(refreshTokenService::revokeToken);
    }

    @Transactional
    public void logoutAll(UserPrincipal userPrincipal) {
        User user = userRepository.findActiveById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        refreshTokenService.revokeByUser(user);
    }

    @Transactional
    public AuthenticationResponse processGoogleOAuth2Login(String email, String name, String googleId, String givenName, String familyName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required for Google OAuth2 login");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required for Google OAuth2 login");
        }
        if (googleId == null || googleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Google ID is required for Google OAuth2 login");
        }
        if (givenName == null || givenName.trim().isEmpty()) {
            throw new IllegalArgumentException("Given name is required for Google OAuth2 login");
        }
        if (familyName == null || familyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Family name is required for Google OAuth2 login");
        }

        User user = userRepository.findActiveByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .givenName(givenName)
                            .familyName(familyName)
                            .googleId(googleId)
                            .isEnabled(true)
                            .build();
                    User savedUser = userRepository.save(newUser);
                    savedUser.setCreatedBy(savedUser.getId());
                    return userRepository.save(savedUser);
                });

        if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
            user.setGoogleId(googleId);
            user = userRepository.save(user);
        }

        List<Role> userRoles = authorizationService.getUserRoles(user.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user, userRoles, userPermissions);
        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        UserWithAuthorizationResponse userWithAuth = userAuthorizationMapper.toUserWithAuthorizationResponse(
                user, userRoles, userPermissions);

        return authResponseMapper.toAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpiration() / 1000,
                userWithAuth
        );
    }

    public UserPrincipal validateAccessToken(String token) {
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));

        List<Role> userRoles = authorizationService.getUserRoles(user.getId());
        List<Permission> userPermissions = authorizationService.getUserPermissions(user.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(user, userRoles, userPermissions);

        if (!jwtUtil.isTokenValid(token, userPrincipal)) {
            throw new RuntimeException("Token is invalid");
        }

        return userPrincipal;
    }
}

