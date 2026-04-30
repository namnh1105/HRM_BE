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

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] Delete Shift API")
class DeleteShiftIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WorkShiftRepository workShiftRepository;

    private WorkShift seedShift;
    private String managerBearerToken;

    @BeforeEach
    void setUpModule() {
        seedShift = workShiftRepository.save(WorkShift.builder()
                .name("Delete Shift")
                .code("DEL-" + UUID.randomUUID().toString().substring(0, 6))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakDuration(1.0)
                .totalHours(8.0)
                .isActive(true)
                .isDeleted(false)
                .build());

        managerBearerToken = bearerTokenByRole("manager");
    }

    @Nested
    @DisplayName("Delete")
    class DeleteCases {

        @Test
        @DisplayName("DELETE /api/v1/work-shifts/{id} -> xóa thành công")
        void deleteShiftSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/work-shifts/{id}", seedShift.getId())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            WorkShift deleted = workShiftRepository.findById(seedShift.getId())
                    .orElseThrow(() -> new AssertionError("Không tìm thấy shift sau delete"));

            assertThat(deleted.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("Shift không tồn tại -> 404")
        void deleteUnknownShiftShouldReturn404() throws Exception {
            mockMvc.perform(delete("/api/v1/work-shifts/{id}", UUID.randomUUID())
                            .header("Authorization", managerBearerToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @Disabled("Chưa có hook mô phỏng DB fail tại service để verify rollback integration")
        @DisplayName("DB fail simulation: không để dirty data")
        void deleteShiftRollbackOnDbFailure() {
        }
    }
}
