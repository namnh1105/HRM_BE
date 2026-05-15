package com.hainam.worksphere.payroll.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoGeneratePayrollRequest {

    private Integer month;

    private Integer year;

    /**
     * Fixed penalty amount applied for each late attendance.
     */
    private Double latePenaltyPerShift;

    /**
     * Fixed allowance added for each employee (if any).
     */
    private Double allowance;

    /**
     * If true, overwrite existing payrolls in DRAFT status.
     */
    private Boolean overwriteExisting;
}
