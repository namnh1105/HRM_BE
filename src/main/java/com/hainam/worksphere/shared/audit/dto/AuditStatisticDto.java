package com.hainam.worksphere.shared.audit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditStatisticDto {
    private String action;
    private Long count;
    private String period;
}
