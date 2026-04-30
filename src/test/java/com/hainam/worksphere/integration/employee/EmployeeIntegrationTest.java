package com.hainam.worksphere.integration.employee;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.integration.BaseIntegrationTest;
import com.hainam.worksphere.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Employee API")
class EmployeeIntegrationTest extends BaseIntegrationTest {

    private static final String EMPLOYEE_ENDPOINT = "/api/v1/employees";

    @Autowired
    private EmployeeRepository employeeRepository;

    private User seedAccount;
    private String managerBearerToken;

    @BeforeEach
    void setUpModule() {
        seedAccount = userRepository.save(User.builder()
                .email("employee-seed-" + UUID.randomUUID() + "@example.com")
                .password(passwordEncoder.encode("Password123"))
                .isEnabled(true)
                .isDeleted(false)
                .build());

        managerBearerToken = bearerTokenByRole("manager");
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("POST /api/v1/employees -> tạo employee gắn account hợp lệ -> 201")
        void createEmployeeWithValidAccountShouldReturn201() throws Exception {
            Map<String, Object> body = buildCreateEmployeeRequest(seedAccount.getId(), "EMP-10001", "employee1@example.com");

            performCreateEmployee(body)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.userId").value(seedAccount.getId().toString()))
                    .andExpect(jsonPath("$.data.employeeCode").value("EMP-10001"));
        }

        @Test
        @DisplayName("Verify DB: user_id trong bảng employees đúng với account vừa tạo")
        void verifyEmployeeUserIdInDatabase() throws Exception {
            Map<String, Object> body = buildCreateEmployeeRequest(seedAccount.getId(), "EMP-10002", "employee2@example.com");

            performCreateEmployee(body)
                    .andExpect(status().isCreated());

            Employee saved = employeeRepository.findActiveByUserId(seedAccount.getId())
                    .orElseThrow(() -> new AssertionError("Không tìm thấy employee theo user_id trong DB"));

            assertThat(saved.getUser()).isNotNull();
            assertThat(saved.getUser().getId()).isEqualTo(seedAccount.getId());
            assertThat(saved.getEmployeeCode()).isEqualTo("EMP-10002");
        }
    }

    @Nested
    @DisplayName("Error path")
    class ErrorPath {

        @Test
        @DisplayName("account_id (userId) không tồn tại -> 404")
        void createEmployeeWithUnknownAccountShouldReturn404() throws Exception {
            Map<String, Object> body = buildCreateEmployeeRequest(UUID.randomUUID(), "EMP-20001", "employee3@example.com");

            performCreateEmployee(body)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("account_id đã được gắn employee rồi -> lỗi unique")
        void createEmployeeWithDuplicatedAccountShouldFail() throws Exception {
            Map<String, Object> first = buildCreateEmployeeRequest(seedAccount.getId(), "EMP-20002", "employee4@example.com");
            Map<String, Object> second = buildCreateEmployeeRequest(seedAccount.getId(), "EMP-20003", "employee5@example.com");

            performCreateEmployee(first).andExpect(status().isCreated());

            performCreateEmployee(second)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Thiếu field bắt buộc -> 400")
        void createEmployeeMissingRequiredFieldShouldReturn400() throws Exception {
            Map<String, Object> body = buildCreateEmployeeRequest(seedAccount.getId(), "EMP-20004", "employee6@example.com");
            body.remove("firstName");

            performCreateEmployee(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @Disabled("Behavior hiện tại trả 400 do unique constraint/runtime exception, chưa map thành 409")
        @DisplayName("account_id đã được gắn employee rồi -> 409")
        void createEmployeeWithDuplicatedAccountShouldReturn409() {
            // Pending theo contract mong muốn.
        }
    }

    private ResultActions performCreateEmployee(Map<String, Object> body) throws Exception {
        return mockMvc.perform(post(EMPLOYEE_ENDPOINT)
                .header("Authorization", managerBearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private Map<String, Object> buildCreateEmployeeRequest(UUID userId, String employeeCode, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("employeeCode", employeeCode);
        body.put("userId", userId);
        body.put("firstName", "Nhan");
        body.put("lastName", "Nguyen");
        body.put("email", email);
        body.put("phone", "0900000000");
        body.put("dateOfBirth", LocalDate.of(1999, 1, 1).toString());
        body.put("gender", "MALE");
        body.put("position", "Staff");
        body.put("joinDate", LocalDate.now().toString());
        return body;
    }
}
