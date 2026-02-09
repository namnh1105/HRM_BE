package com.hainam.worksphere.workshift.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkShiftRequest {

    @Size(max = 100, message = "Work shift name must not exceed 100 characters")
    private String name;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("break_duration")
    private Double breakDuration;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_night_shift")
    private Boolean isNightShift;
}
