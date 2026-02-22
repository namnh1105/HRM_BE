package com.hainam.worksphere.workshift.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignWorkShiftRequest {

    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;
}
