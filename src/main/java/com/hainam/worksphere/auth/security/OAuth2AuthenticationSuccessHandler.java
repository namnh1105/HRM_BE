package com.hainam.worksphere.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hainam.worksphere.auth.service.AuthenticationService;
import com.hainam.worksphere.shared.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(@Lazy AuthenticationService authenticationService,
                                            ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Extract user information from Google OAuth2 response
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub");
            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");

            log.info("Google OAuth2 login successful for email: {}", email);

            // Process OAuth2 login through AuthenticationService
            var authResponse = authenticationService.processGoogleOAuth2Login(email, name, googleId, givenName, familyName);

            // Return JSON response instead of redirect
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("data", authResponse);
            responseBody.put("message", "Google login successful");

            response.getWriter().write(objectMapper.writeValueAsString(responseBody));

        } catch (Exception e) {
            log.error("Error processing Google OAuth2 login", e);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            ApiResponse<Object> errorResponse = ApiResponse.builder()
                    .success(false)
                    .message("Google login failed: " + e.getMessage())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}

