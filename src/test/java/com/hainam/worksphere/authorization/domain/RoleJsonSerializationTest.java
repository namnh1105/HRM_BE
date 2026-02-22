package com.hainam.worksphere.authorization.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hainam.worksphere.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Role JSON Serialization Tests")
class RoleJsonSerializationTest extends BaseUnitTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize Role with LocalDateTime fields to JSON")
    void shouldSerializeRoleWithLocalDateTimeToJson() throws Exception {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 10, 8, 0, 0, 123000000);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 2, 10, 9, 30, 45, 567000000);

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .code("TEST_ROLE")
                .displayName("Test Role")
                .description("A test role for JSON serialization")
                .isSystem(false)
                .isActive(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        String json = objectMapper.writeValueAsString(role);

        // Then
        assertAll(
                () -> assertThat(json).contains("\"code\":\"TEST_ROLE\""),
                () -> assertThat(json).contains("\"displayName\":\"Test Role\""),
                () -> assertThat(json).contains("\"createdAt\":\"2026-02-10T08:00:00.123\""),
                () -> assertThat(json).contains("\"updatedAt\":\"2026-02-10T09:30:45.567\""),
                () -> assertThat(json).doesNotContain("\"createdAt\":[") // Should not be an array (timestamp)
        );

        System.out.println("Serialized JSON: " + json);
    }

    @Test
    @DisplayName("Should deserialize JSON with LocalDateTime fields to Role")
    void shouldDeserializeJsonWithLocalDateTimeToRole() throws Exception {
        // Given
        String json = """
                {
                    "id": "550e8400-e29b-41d4-a716-446655440001",
                    "code": "DESERIALIZE_TEST",
                    "displayName": "Deserialize Test Role",
                    "description": "A role for deserialization testing",
                    "isSystem": false,
                    "isActive": true,
                    "createdAt": "2026-02-10T10:15:30.789",
                    "updatedAt": "2026-02-10T11:20:35.123",
                    "rolePermissions": []
                }
                """;

        // When
        Role role = objectMapper.readValue(json, Role.class);

        // Then
        assertAll(
                () -> assertThat(role.getCode()).isEqualTo("DESERIALIZE_TEST"),
                () -> assertThat(role.getDisplayName()).isEqualTo("Deserialize Test Role"),
                () -> assertThat(role.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 10, 15, 30, 789000000)),
                () -> assertThat(role.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 11, 20, 35, 123000000)),
                () -> assertThat(role.getIsActive()).isTrue(),
                () -> assertThat(role.getIsSystem()).isFalse()
        );
    }
}
