package com.hainam.worksphere;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base test class for unit tests with common configurations
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {

    // Common test utilities and constants
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_NAME = "Test User";
    protected static final String TEST_PASSWORD = "testPassword123";
    protected static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    protected static final UUID TEST_ROLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    protected static final String TEST_IP_ADDRESS = "192.168.1.1";
    protected static final String TEST_USER_AGENT = "Mozilla/5.0 Test Agent";

    protected ObjectMapper objectMapper;

    @BeforeEach
    void baseSetUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to mock Spring Security context
     */
    protected void mockSecurityContext(String username) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Helper method to convert object to JSON string
     */
    protected String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Helper method to create test timestamp
     */
    protected Instant getTestTimestamp() {
        return Instant.parse("2024-01-01T12:00:00Z");
    }
}

