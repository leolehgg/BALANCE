package com.wellbeing.deviceusage.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyUsageStatsDto {
    private Year year;
    private Map<Month, MonthlyUsageStatsDto> monthlyStats;
    private long totalUsageSeconds;
    private long productiveTimeSeconds;
    private long nonProductiveTimeSeconds;
    private List<AppUsageDto> topApplications;
    private Map<Month, Long> monthlyUsageTrend; // Mes -> segundos totales
}