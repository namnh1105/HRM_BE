package com.hainam.worksphere.leave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {

    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("leave_type")
    private String leaveType;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("total_days")
    private Double totalDays;

    private String reason;

    private String status;

    @JsonProperty("approver_id")
    private UUID approverId;

    @JsonProperty("approver_name")
    private String approverName;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("approver_comment")
    private String approverComment;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
