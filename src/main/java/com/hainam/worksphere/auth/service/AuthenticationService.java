package com.hainam.worksphere.auth.service;

import com.hainam.worksphere.auth.domain.RefreshToken;
import com.hainam.worksphere.auth.dto.request.*;
import com.hainam.worksphere.auth.dto.response.AuthenticationResponse;
import com.hainam.worksphere.auth.dto.response.TokenResponse;
import com.hainam.worksphere.auth.mapper.AuthMapper;
import com.hainam.worksphere.auth.mapper.AuthResponseMapper;
import com.hainam.worksphere.auth.security.UserPrincipal;
import com.hainam.worksphere.auth.util.JwtUtil;
import com.hainam.worksphere.shared.exception.EmailAlreadyExistsException;
import com.hainam.worksphere.shared.exception.InvalidCredentialsException;
import com.hainam.worksphere.shared.exception.RefreshTokenException;
import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.user.domain.User;
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

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw EmailAlreadyExistsException.withEmail(request.getEmail());
        }

        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);

        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return authResponseMapper.toAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessTokenExpiration() / 1000,
                userMapper.toUserResponse(savedUser)
        );
    }


    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));

            String accessToken = jwtUtil.generateAccessToken(userPrincipal);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return authResponseMapper.toAuthenticationResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtUtil.getAccessTokenExpiration() / 1000,
                    userMapper.toUserResponse(user)
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
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
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
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId().toString()));
        refreshTokenService.revokeByUser(user);
    }
}

