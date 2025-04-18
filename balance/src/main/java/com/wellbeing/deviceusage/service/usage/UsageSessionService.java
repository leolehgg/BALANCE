package com.wellbeing.deviceusage.service.usage;

import com.wellbeing.deviceusage.dto.usage.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface UsageSessionService {
    UsageSessionDto startSession(StartSessionRequest request);

    UsageSessionDto endSession(Long sessionId, EndSessionRequest request);

    Page<UsageSessionDto> getUserSessions(int page, int size, Long deviceId,
                                          LocalDate startDate, LocalDate endDate);

    DailyUsageStatsDto getDailyUsageStats(LocalDate date, Long deviceId);

    WeeklyUsageStatsDto getWeeklyUsageStats(LocalDate startOfWeek, Long deviceId);
}