package com.hainam.worksphere.integration.workshift;

import com.hainam.worksphere.integration.BaseIntegrationTest;
import com.hainam.worksphere.workshift.domain.WorkShift;
import com.hainam.worksphere.workshift.repository.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Update Shift API")
class UpdateShiftIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WorkShiftRepository workShiftRepository;

    private WorkShift seedShift;
    private String managerBearerToken;

    @BeforeEach
    void setUpModule() {
        seedShift = workShiftRepository.save(WorkShift.builder()
                .name("Initial Shift")
                .code("UPD-" + UUID.randomUUID().toString().substring(0, 6))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(1.0)
                .totalHours(8.0)
                .isActive(true)
                .isNightShift(false)
                .isDeleted(false)
                .build());

        managerBearerToken = bearerTokenByRole("manager");
    }

    @Nested
    @DisplayName("Update")
    class UpdateCases {

        @Test
        @DisplayName("PUT /api/v1/work-shifts/{id} -> update thời gian thành công -> 200")
        void updateShiftTimeShouldReturn200() throws Exception {
            Map<String, Object> body = buildUpdateWorkShiftRequest("Updated Shift", "09:00:00", "18:00:00");

            mockMvc.perform(put("/api/v1/work-shifts/{id}", seedShift.getId())
                            .header("Authorization", managerBearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.startTime").value("09:00:00"))
                    .andExpect(jsonPath("$.data.endTime").value("18:00:00"));

            WorkShift updated = workShiftRepository.findActiveById(seedShift.getId())
                    .orElseThrow(() -> new AssertionError("Không tìm thấy shift sau update"));
            assertThat(updated.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(updated.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("Shift không tồn tại -> 404")
        void updateUnknownShiftShouldReturn404() throws Exception {
            Map<String, Object> body = buildUpdateWorkShiftRequest("Updated Shift", "10:00:00", "19:00:00");

            mockMvc.perform(put("/api/v1/work-shifts/{id}", UUID.randomUUID())
                            .header("Authorization", managerBearerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @Disabled("Service hiện tại chưa hỗ trợ đổi employee cho assignment ở endpoint update shift")
        @DisplayName("Đổi sang employee khác (active) -> 200")
        void changeToAnotherEmployeeShouldReturn200() {
        }

        @Test
        @Disabled("Service hiện tại chưa check conflict overlap khi update shift")
        @DisplayName("Ca mới bị xung đột với ca đang có -> 409")
        void updateToConflictShiftShouldReturn409() {
        }

        @Test
        @Disabled("Service hiện tại chưa có rule employee inactive ở update shift")
        @DisplayName("Employee mới đang inactive -> 422")
        void updateToInactiveEmployeeShouldReturn422() {
        }
    }

    private Map<String, Object> buildUpdateWorkShiftRequest(String name, String startTime, String endTime) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("startTime", startTime);
        body.put("endTime", endTime);
        body.put("breakDuration", 1.0);
        body.put("isActive", true);
        return body;
    }
}
