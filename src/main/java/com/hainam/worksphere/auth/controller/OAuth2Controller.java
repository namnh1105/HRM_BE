package com.hainam.worksphere.auth.controller;

import com.hainam.worksphere.auth.service.AuthenticationService;
import com.hainam.worksphere.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthenticationService authenticationService;

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @GetMapping("/google")
    public RedirectView googleLogin() {
        return new RedirectView("/oauth2/authorization/google");
    }

    @GetMapping("/google/url")
    public ApiResponse<String> getGoogleLoginUrl() {
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        String googleAuthUrl = baseUrl + "/oauth2/authorization/google";

        return ApiResponse.<String>builder()
                .success(true)
                .message("Google OAuth2 authorization URL. Note: Redirect URI should be configured as: " +
                        baseUrl + "/login/oauth2/code/google")
                .data(googleAuthUrl)
                .build();
    }

    /**
     * SPA OAuth2 flow: Exchange authorization code for JWT tokens
     */
    @PostMapping("/google/callback")
    public ApiResponse<Object> handleGoogleCallback(@RequestParam("code") String code,
                                                   @RequestParam(value = "state", required = false) String state) {
        try {
            var authResponse = authenticationService.processGoogleOAuth2Callback(code, state);

            return ApiResponse.builder()
                    .success(true)
                    .message("Google login successful")
                    .data(authResponse)
                    .build();
        } catch (Exception e) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Google login failed: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/google/config")
    public ApiResponse<Object> getGoogleOAuth2Config() {
        String baseUrl = "http://localhost:" + serverPort + contextPath;

        return ApiResponse.builder()
                .success(true)
                .message("Google OAuth2 Configuration")
                .data(Map.of(
                    "authorizationUrl", baseUrl + "/oauth2/authorization/google",
                    "redirectUri", baseUrl + "/login/oauth2/code/google",
                    "currentPort", serverPort,
                    "contextPath", contextPath,
                    "note", "Make sure the redirectUri is registered in Google Cloud Console"
                ))
                .build();
    }
}
