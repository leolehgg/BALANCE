package com.wellbeing.deviceusage.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyUsageStatsDto {
    private LocalDate date;
    private long totalUsageSeconds;
    private Map<String, Long> applicationUsage;
    private Map<String, Long> categoryUsage;
    private Map<String, Long> deviceUsage;
    private long productiveTimeSeconds;
    private long nonProductiveTimeSeconds;
}