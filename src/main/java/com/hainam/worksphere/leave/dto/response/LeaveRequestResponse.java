package com.hainam.worksphere.leave.dto.response;

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

    private UUID employeeId;

    private String employeeName;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double totalDays;

    private String reason;

    private String status;

    private UUID approverId;

    private String approverName;

    private LocalDateTime approvedAt;

    private String approverComment;

    private String attachmentUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
