package com.hainam.worksphere.workshift.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftResponse {

    private UUID id;

    private String name;

    private String code;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("break_duration")
    private Double breakDuration;

    @JsonProperty("total_hours")
    private Double totalHours;

    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_night_shift")
    private Boolean isNightShift;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
