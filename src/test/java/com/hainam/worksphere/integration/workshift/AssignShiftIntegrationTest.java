package com.hainam.worksphere.integration.workshift;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.integration.BaseIntegrationTest;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Assign Shift API")
class AssignShiftIntegrationTest extends BaseIntegrationTest {

    private static final String ASSIGN_ENDPOINT = "/api/v1/employee-work-shifts";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private EmployeeWorkShiftRepository employeeWorkShiftRepository;

    private Employee activeEmployee;
    private WorkShift defaultShift;
    private String managerBearerToken;
    private String staffBearerToken;

    @BeforeEach
    void setUpModule() {
        User user = userRepository.save(User.builder()
                .email("assign-target-" + UUID.randomUUID() + "@example.com")
                .password(passwordEncoder.encode("Password123"))
                .isEnabled(true)
                .isDeleted(false)
                .build());

        activeEmployee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-AS-" + UUID.randomUUID().toString().substring(0, 6))
                .firstName("Target")
                .lastName("Employee")
                .fullName("Employee Target")
                .email("employee-assign-" + UUID.randomUUID() + "@example.com")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .user(user)
                .isDeleted(false)
                .build());

        defaultShift = workShiftRepository.save(WorkShift.builder()
                .name("Morning Shift")
                .code("SHIFT-" + UUID.randomUUID().toString().substring(0, 6))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(1.0)
                .totalHours(8.0)
                .isActive(true)
                .isNightShift(false)
                .isDeleted(false)
                .build());

        managerBearerToken = bearerTokenByRole("manager");
        staffBearerToken = bearerTokenByRole("staff");
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("POST assign 1 employee vào ca -> 201")
        void assignOneEmployeeToShiftShouldReturn201() throws Exception {
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), LocalDate.now().plusDays(1));

            performAssign(body, managerBearerToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.employeeId").value(activeEmployee.getId().toString()))
                    .andExpect(jsonPath("$.data.workShiftId").value(defaultShift.getId().toString()));
        }

        @Test
        @DisplayName("Assign nhiều employee khác nhau vào cùng 1 ca -> 201 mỗi case")
        void assignMultipleEmployeesToSameShiftShouldReturn201EachCase() throws Exception {
            Employee employee2 = employeeRepository.save(Employee.builder()
                    .employeeCode("EMP-AS-2-" + UUID.randomUUID().toString().substring(0, 5))
                    .firstName("Two")
                    .lastName("Employee")
                    .fullName("Employee Two")
                    .email("employee2-assign-" + UUID.randomUUID() + "@example.com")
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .isDeleted(false)
                    .build());

            LocalDate date = LocalDate.now().plusDays(1);

            performAssign(buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), date), managerBearerToken)
                    .andExpect(status().isCreated());

            performAssign(buildAssignRequest(employee2.getId(), defaultShift.getId(), date), managerBearerToken)
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Response body có đủ employee_id, shift_id, start_time, end_time")
        void responseShouldContainRequiredFields() throws Exception {
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), LocalDate.now().plusDays(2));

            performAssign(body, managerBearerToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.employeeId").exists())
                    .andExpect(jsonPath("$.data.workShiftId").exists())
                    .andExpect(jsonPath("$.data.shiftStartTime").value("08:00:00"))
                    .andExpect(jsonPath("$.data.shiftEndTime").value("17:00:00"));
        }
    }

    @Nested
    @DisplayName("Error path")
    class ErrorPath {

        @Test
        @DisplayName("shift_id không tồn tại -> 404")
        void assignWithUnknownShiftShouldReturn404() throws Exception {
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), UUID.randomUUID(), LocalDate.now().plusDays(1));

            performAssign(body, managerBearerToken)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("employee_id không tồn tại -> 404")
        void assignWithUnknownEmployeeShouldReturn404() throws Exception {
            Map<String, Object> body = buildAssignRequest(UUID.randomUUID(), defaultShift.getId(), LocalDate.now().plusDays(1));

            performAssign(body, managerBearerToken)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Assign trùng employee + shift + date -> 400")
        void assignDuplicatedShouldReturn400() throws Exception {
            LocalDate date = LocalDate.now().plusDays(1);
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), date);

            performAssign(body, managerBearerToken).andExpect(status().isCreated());

            performAssign(body, managerBearerToken)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Staff gọi assign -> 403")
        void staffAssignShouldReturn403() throws Exception {
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), LocalDate.now().plusDays(1));

            performAssign(body, staffBearerToken)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Token hết hạn -> 401")
        void expiredTokenShouldReturn401() throws Exception {
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), LocalDate.now().plusDays(1));

            performAssign(body, bearerExpiredTokenByRole("manager"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Business/Conflict pending")
    class PendingBusinessRules {

        @Test
        @Disabled("Service hiện tại chưa validate employee inactive -> 422")
        @DisplayName("Employee inactive -> 422")
        void inactiveEmployeeShouldReturn422() {
        }

        @Test
        @Disabled("Service hiện tại chưa check overlap shift -> 409")
        @DisplayName("Employee đã có ca chồng giờ -> 409")
        void overlappingShiftShouldReturn409() {
        }

        @Test
        @Disabled("Service hiện tại chưa enforce rule ca quá khứ")
        @DisplayName("Assign ca trong quá khứ -> 400/422")
        void assignPastDateShouldFail() {
        }

        @Test
        @DisplayName("Night shift 22:00->02:00 lưu và trả đúng")
        void nightShiftShouldPersistCorrectly() throws Exception {
            WorkShift nightShift = workShiftRepository.save(WorkShift.builder()
                    .name("Night Shift")
                    .code("NIGHT-" + UUID.randomUUID().toString().substring(0, 6))
                    .startTime(LocalTime.of(22, 0))
                    .endTime(LocalTime.of(2, 0))
                    .breakDuration(0.5)
                    .totalHours(3.5)
                    .isActive(true)
                    .isNightShift(true)
                    .isDeleted(false)
                    .build());

            LocalDate date = LocalDate.now().plusDays(1);
            performAssign(buildAssignRequest(activeEmployee.getId(), nightShift.getId(), date), managerBearerToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.shiftStartTime").value("22:00:00"))
                    .andExpect(jsonPath("$.data.shiftEndTime").value("02:00:00"));

            List<EmployeeWorkShift> saved = employeeWorkShiftRepository.findActiveByEmployeeIdAndDate(activeEmployee.getId(), date);
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).getWorkShift().getStartTime()).isEqualTo(LocalTime.of(22, 0));
            assertThat(saved.get(0).getWorkShift().getEndTime()).isEqualTo(LocalTime.of(2, 0));
        }

        @Test
        @Disabled("Cần unique constraint/locking để đảm bảo race condition 1 success + 1 fail")
        @DisplayName("Race condition concurrent assign cùng employee-cùng ca")
        void raceConditionConcurrentAssign() throws Exception {
            LocalDate date = LocalDate.now().plusDays(1);
            Map<String, Object> body = buildAssignRequest(activeEmployee.getId(), defaultShift.getId(), date);

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            Callable<Integer> task = () -> {
                try {
                    performAssign(body, managerBearerToken).andReturn().getResponse().getStatus();
                    return 201;
                } catch (Exception e) {
                    return 500;
                }
            };

            Future<Integer> f1 = executorService.submit(task);
            Future<Integer> f2 = executorService.submit(task);

            int s1 = f1.get();
            int s2 = f2.get();

            assertThat(List.of(s1, s2)).containsExactlyInAnyOrder(201, 409);
            executorService.shutdown();
        }
    }

    private ResultActions performAssign(Map<String, Object> body, String bearerToken) throws Exception {
        return mockMvc.perform(post(ASSIGN_ENDPOINT)
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private Map<String, Object> buildAssignRequest(UUID employeeId, UUID workShiftId, LocalDate date) {
        Map<String, Object> body = new HashMap<>();
        body.put("employeeId", employeeId);
        body.put("workShiftId", workShiftId);
        body.put("date", date.toString());
        return body;
    }
}
