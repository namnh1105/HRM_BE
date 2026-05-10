package com.hainam.worksphere.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private long totalAccounts;
    private long activeAccounts;
    private long inactiveAccounts;
    private long deletedAccounts;
}
