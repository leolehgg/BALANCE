package com.wellbeing.deviceusage.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyUsageStatsDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<DayOfWeek, DailyUsageStatsDto> dailyStats;
    private long totalUsageSeconds;
    private long productiveTimeSeconds;
    private long nonProductiveTimeSeconds;
    private List<AppUsageDto> topApplications;
}