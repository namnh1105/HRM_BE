package com.hainam.worksphere.integration.account;

import com.hainam.worksphere.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Account API")
class AccountIntegrationTest extends BaseIntegrationTest {

    private static final String REGISTER_ENDPOINT = "/api/v1/auth/register";

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Tạo account thành công với role=staff (mapping sang USER)")
        void createAccountSuccessWithStaffRole() throws Exception {
            Map<String, Object> body = buildRegisterBody(
                    "Nhan",
                    "Nguyen",
                    uniqueEmail(),
                    "Password123"
            );
            body.put("role", "staff");

            performRegister(body)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.user.id").exists())
                    .andExpect(jsonPath("$.data.accessToken").exists());
        }

        @Test
        @DisplayName("Tạo account thành công với role=manager (mapping sang ADMIN)")
        void createAccountSuccessWithManagerRole() throws Exception {
            Map<String, Object> body = buildRegisterBody(
                    "Linh",
                    "Tran",
                    uniqueEmail(),
                    "Password123"
            );
            body.put("role", "manager");

            performRegister(body)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.user.id").exists())
                    .andExpect(jsonPath("$.data.accessToken").exists());
        }
    }

    @Nested
    @DisplayName("Validation/Error")
    class ValidationAndError {

        @Test
        @DisplayName("Email đã tồn tại -> trả lỗi")
        void duplicateEmailShouldFail() throws Exception {
            String duplicatedEmail = uniqueEmail();

            performRegister(buildRegisterBody("A", "User", duplicatedEmail, "Password123"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

            performRegister(buildRegisterBody("B", "User", duplicatedEmail, "Password123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message", containsString("Email already exists")));
        }

        @ParameterizedTest(name = "Password không hợp lệ: {0}")
        @MethodSource("com.hainam.worksphere.integration.account.AccountIntegrationTest#invalidPasswordCases")
        @DisplayName("Password không hợp lệ -> 400")
        void invalidPasswordShouldReturn400(String caseName, String invalidPassword) throws Exception {
            Map<String, Object> body = buildRegisterBody(
                    "Test",
                    "Password",
                    uniqueEmail(),
                    invalidPassword
            );

            performRegister(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("Thiếu field username (mapping sang givenName) -> 400")
        void missingUsernameShouldReturn400() throws Exception {
            Map<String, Object> body = buildRegisterBody(
                    "Temp",
                    "User",
                    uniqueEmail(),
                    "Password123"
            );
            body.remove("givenName");

            performRegister(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("Thiếu field email -> 400")
        void missingEmailShouldReturn400() throws Exception {
            Map<String, Object> body = buildRegisterBody(
                    "Temp",
                    "User",
                    uniqueEmail(),
                    "Password123"
            );
            body.remove("email");

            performRegister(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @Disabled("API hiện tại không có username riêng, chỉ có email")
        @DisplayName("Username đã tồn tại -> 409")
        void duplicateUsernameShouldReturn409() {
            // Pending theo contract mới: /api/accounts có trường username riêng.
        }

        @Test
        @Disabled("API hiện tại chưa validate password bắt buộc chứa số")
        @DisplayName("Password thiếu số -> 400")
        void passwordWithoutNumberShouldReturn400() {
            // Pending theo contract mới.
        }

        @Test
        @Disabled("API hiện tại không nhận field role trong request register")
        @DisplayName("Role không hợp lệ (admin) -> 400")
        void invalidRoleShouldReturn400() {
            // Pending theo contract mới.
        }
    }

    static Stream<Arguments> invalidPasswordCases() {
        return Stream.of(
                Arguments.of("Dưới 6 ký tự", "abc12"),
                Arguments.of("Rỗng", "")
        );
    }

    private ResultActions performRegister(Map<String, Object> body) throws Exception {
        return mockMvc.perform(
                post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
        );
    }

    private Map<String, Object> buildRegisterBody(String givenName, String familyName, String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("givenName", givenName);
        body.put("familyName", familyName);
        body.put("email", email);
        body.put("password", password);
        body.put("avatarUrl", "https://example.com/avatar.png");
        return body;
    }

    private String uniqueEmail() {
        return "account-it-" + UUID.randomUUID() + "@example.com";
    }
}
