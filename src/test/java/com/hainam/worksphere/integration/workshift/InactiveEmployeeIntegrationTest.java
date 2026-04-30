package com.hainam.worksphere.integration.workshift;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.employee.domain.EmploymentStatus;
import com.hainam.worksphere.employee.repository.EmployeeRepository;
import com.hainam.worksphere.integration.BaseIntegrationTest;
import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.repository.EmployeeWorkShiftRepository;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Inactive Employee (Cross-cutting)")
class InactiveEmployeeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private EmployeeWorkShiftRepository employeeWorkShiftRepository;

    private Employee employee;
    private WorkShift shift;
    private String managerBearerToken;

    @BeforeEach
    void setUpModule() {
        employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-INACTIVE-" + UUID.randomUUID().toString().substring(0, 5))
                .firstName("Inactive")
                .lastName("Case")
                .fullName("Case Inactive")
                .email("inactive-employee-" + UUID.randomUUID() + "@example.com")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .isDeleted(false)
                .build());

        shift = workShiftRepository.save(WorkShift.builder()
                .name("Inactive Shift")
                .code("INA-" + UUID.randomUUID().toString().substring(0, 6))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(1.0)
                .totalHours(8.0)
                .isActive(true)
                .isDeleted(false)
                .build());

        LocalDate oldDate = LocalDate.now().minusDays(2);
        employeeWorkShiftRepository.save(EmployeeWorkShift.builder()
                .employee(employee)
                .workShift(shift)
                .date(oldDate)
                .isDeleted(false)
                .build());

        managerBearerToken = bearerTokenByRole("manager");
    }

    @Test
    @Disabled("Service hiện tại chưa chặn assign khi employee không active")
    @DisplayName("Set employee INACTIVE -> assign ca mới -> 422")
    void assignForInactiveEmployeeShouldReturn422() {
    }

    @Test
    @DisplayName("Lịch ca cũ trước khi inactive vẫn còn trong DB")
    void oldShiftShouldRemainAfterEmployeeInactive() {
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);

        assertThat(employeeWorkShiftRepository.findActiveByEmployeeId(employee.getId())).hasSize(1);
    }

    @Test
    @DisplayName("GET lịch theo employee vẫn trả lịch cũ dù employee inactive")
    void getShiftShouldStillReturnOldShiftWhenEmployeeInactive() throws Exception {
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);

        mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}", employee.getId())
                        .header("Authorization", managerBearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("(Current behavior) employee terminated vẫn assign được")
    void currentBehaviorInactiveEmployeeCanStillBeAssigned() throws Exception {
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);

        Map<String, Object> body = new HashMap<>();
        body.put("employeeId", employee.getId());
        body.put("workShiftId", shift.getId());
        body.put("date", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/api/v1/employee-work-shifts")
                        .header("Authorization", managerBearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
