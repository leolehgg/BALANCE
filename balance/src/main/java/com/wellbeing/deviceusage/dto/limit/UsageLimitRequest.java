package com.wellbeing.deviceusage.dto.limit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsageLimitRequest {
    private Long categoryId;
    private Long applicationId;

    @NotNull
    @Min(1)
    private Integer dailyLimitMinutes;

    private String daysOfWeek;
}