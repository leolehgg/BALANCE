package com.wellbeing.deviceusage.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyUsageStatsDto {
    private YearMonth month;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<Integer, DailyUsageStatsDto> dailyStats; // Día del mes -> estadísticas
    private long totalUsageSeconds;
    private long productiveTimeSeconds;
    private long nonProductiveTimeSeconds;
    private List<AppUsageDto> topApplications;
    private Map<Integer, Long> dailyUsageTrend; // Día del mes -> segundos totales
}