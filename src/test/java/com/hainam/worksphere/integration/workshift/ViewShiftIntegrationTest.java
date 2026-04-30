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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] View Shift API")
class ViewShiftIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private EmployeeWorkShiftRepository employeeWorkShiftRepository;

    private Employee employee;
    private LocalDate targetDate;
    private String managerBearerToken;

    @BeforeEach
    void setUpModule() {
        employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-VIEW-" + UUID.randomUUID().toString().substring(0, 6))
                .firstName("View")
                .lastName("Employee")
                .fullName("Employee View")
                .email("view-employee-" + UUID.randomUUID() + "@example.com")
                .employmentStatus(EmploymentStatus.ACTIVE)
                .isDeleted(false)
                .build());

        WorkShift shift1 = workShiftRepository.save(WorkShift.builder()
                .name("S1")
                .code("S1-" + UUID.randomUUID().toString().substring(0, 5))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .breakDuration(0.5)
                .totalHours(3.5)
                .isActive(true)
                .isDeleted(false)
                .build());

        WorkShift shift2 = workShiftRepository.save(WorkShift.builder()
                .name("S2")
                .code("S2-" + UUID.randomUUID().toString().substring(0, 5))
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(0.5)
                .totalHours(3.5)
                .isActive(true)
                .isDeleted(false)
                .build());

        WorkShift shift3 = workShiftRepository.save(WorkShift.builder()
                .name("S3")
                .code("S3-" + UUID.randomUUID().toString().substring(0, 5))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(22, 0))
                .breakDuration(0.5)
                .totalHours(3.5)
                .isActive(true)
                .isDeleted(false)
                .build());

        targetDate = LocalDate.of(2025, 1, 15);
        employeeWorkShiftRepository.save(EmployeeWorkShift.builder().employee(employee).workShift(shift1).date(targetDate).isDeleted(false).build());
        employeeWorkShiftRepository.save(EmployeeWorkShift.builder().employee(employee).workShift(shift2).date(targetDate).isDeleted(false).build());
        employeeWorkShiftRepository.save(EmployeeWorkShift.builder().employee(employee).workShift(shift3).date(targetDate.plusDays(1)).isDeleted(false).build());

        managerBearerToken = bearerTokenByRole("manager");
    }

    @Nested
    @DisplayName("View/filter")
    class ViewFilter {

        @Test
        @DisplayName("GET employee+date -> trả đúng danh sách ca ngày đó")
        void getByEmployeeAndDateShouldReturnCorrectShifts() throws Exception {
            mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}/date", employee.getId())
                            .param("date", targetDate.toString())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET employee -> trả đúng lịch employee")
        void getByEmployeeShouldReturnAllEmployeeSchedules() throws Exception {
            mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}", employee.getId())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("Filter kết hợp employee + date hoạt động đúng")
        void combinedFilterShouldWorkCorrectly() throws Exception {
            mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}/date", employee.getId())
                            .param("date", targetDate.plusDays(1).toString())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("Ngày không có ca -> 200, data=[]")
        void noShiftOnDateShouldReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}/date", employee.getId())
                            .param("date", targetDate.plusDays(10).toString())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("employee_id không tồn tại -> 404")
        void unknownEmployeeShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/v1/employee-work-shifts/employee/{employeeId}", UUID.randomUUID())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
