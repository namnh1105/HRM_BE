package com.hainam.worksphere.leave.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.leave.domain.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequestDto {

    @NotNull(message = "Leave type is required")
    @JsonProperty("leave_type")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    @JsonProperty("start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonProperty("end_date")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;

    @JsonProperty("attachment_url")
    private String attachmentUrl;
}
