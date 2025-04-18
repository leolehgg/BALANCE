package com.wellbeing.deviceusage.dto.limit;

import lombok.Data;

@Data
public class UsageLimitDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long applicationId;
    private String applicationName;
    private Integer dailyLimitMinutes;
    private String daysOfWeek;
}