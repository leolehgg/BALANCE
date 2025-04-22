package com.wellbeing.deviceusage.service.usage;

import com.wellbeing.deviceusage.dto.usage.*;
import com.wellbeing.deviceusage.exception.ResourceNotFoundException;
import com.wellbeing.deviceusage.model.Application;
import com.wellbeing.deviceusage.model.Device;
import com.wellbeing.deviceusage.model.UsageSession;
import com.wellbeing.deviceusage.model.User;
import com.wellbeing.deviceusage.repository.ApplicationRepository;
import com.wellbeing.deviceusage.repository.DeviceRepository;
import com.wellbeing.deviceusage.repository.UsageSessionRepository;
import com.wellbeing.deviceusage.service.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsageSessionServiceImpl implements UsageSessionService {

    @Autowired
    private UsageSessionRepository usageSessionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UsageSessionDto startSession(StartSessionRequest request) {
        User currentUser = userService.getCurrentUser();
        Device device = deviceRepository.findByIdAndUser(request.getDeviceId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

        Application application = applicationRepository.findByPackageName(request.getPackageName())
                .orElseGet(() -> {
                    Application newApp = Application.builder()
                            .name(request.getApplicationName())
                            .packageName(request.getPackageName())
                            .productive(false)
                            .build();
                    return applicationRepository.save(newApp);
                });

        UsageSession session = UsageSession.builder()
                .device(device)
                .application(application)
                .startTime(LocalDateTime.now())
                .synchronized_(false)
                .build();

        UsageSession savedSession = usageSessionRepository.save(session);
        return toUsageSessionDto(savedSession);
    }

    @Override
    public UsageSessionDto endSession(Long sessionId, EndSessionRequest request) {
        User currentUser = userService.getCurrentUser();
        UsageSession session = usageSessionRepository.findByIdAndDeviceUser(sessionId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found or not authorized"));

        LocalDateTime endTime = request.getEndTime();
        session.setEndTime(endTime);
        long durationSeconds = ChronoUnit.SECONDS.between(session.getStartTime(), endTime);
        session.setDurationSeconds(durationSeconds);

        UsageSession updatedSession = usageSessionRepository.save(session);
        return toUsageSessionDto(updatedSession);
    }

    @Override
    public Page<UsageSessionDto> getUserSessions(int page, int size, Long deviceId,
                                                 LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();

        LocalDateTime startDateTime = startDate != null ?
                startDate.atStartOfDay() : LocalDate.now().atStartOfDay();

        LocalDateTime endDateTime = endDate != null ?
                endDate.plusDays(1).atStartOfDay() : LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        Page<UsageSession> sessions;
        if (deviceId != null) {
            deviceRepository.findByIdAndUser(deviceId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

            sessions = usageSessionRepository.findByDeviceIdAndStartTimeBetween(
                    deviceId, startDateTime, endDateTime, pageable);
        } else {
            sessions = usageSessionRepository.findByDeviceUserAndStartTimeBetween(
                    currentUser, startDateTime, endDateTime, pageable);
        }

        return sessions.map(this::toUsageSessionDto);
    }

    // Méto_do para mapear manualmente UsageSession a UsageSessionDto
    private UsageSessionDto toUsageSessionDto(UsageSession session) {
        UsageSessionDto dto = new UsageSessionDto();
        dto.setId(session.getId());
        dto.setClientId(session.getClientId());
        dto.setApplicationName(session.getApplication() != null ? session.getApplication().getName() : null);
        dto.setPackageName(session.getApplication() != null ? session.getApplication().getPackageName() : null);
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setDurationSeconds(session.getDurationSeconds());
        return dto;
    }

    @Override
    public WeeklyUsageStatsDto getWeeklyUsageStats(LocalDate startOfWeek, Long deviceId) {
        User currentUser = userService.getCurrentUser();
        Map<DayOfWeek, DailyUsageStatsDto> dailyStats = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startOfWeek.plusDays(i);
            DailyUsageStatsDto stats = getDailyUsageStats(currentDate, deviceId);
            dailyStats.put(currentDate.getDayOfWeek(), stats);
        }

        long totalWeeklyUsageSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        Map<String, Long> appUsageTime = new HashMap<>();
        dailyStats.values().forEach(day -> {
            day.getApplicationUsage().forEach((app, time) -> {
                appUsageTime.merge(app, time, Long::sum);
            });
        });

        List<AppUsageDto> topApps = appUsageTime.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new AppUsageDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return WeeklyUsageStatsDto.builder()
                .startDate(startOfWeek)
                .endDate(startOfWeek.plusDays(6))
                .dailyStats(dailyStats)
                .totalUsageSeconds(totalWeeklyUsageSeconds)
                .productiveTimeSeconds(productiveTimeSeconds)
                .nonProductiveTimeSeconds(totalWeeklyUsageSeconds - productiveTimeSeconds)
                .topApplications(topApps)
                .build();
    }

    @Override
    public DailyUsageStatsDto getDailyUsageStats(LocalDate date, Long deviceId) {
        User currentUser = userService.getCurrentUser();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<UsageSession> sessions;
        if (deviceId != null) {
            deviceRepository.findByIdAndUser(deviceId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));
            sessions = usageSessionRepository.findByDeviceIdAndStartTimeBetweenAndEndTimeIsNotNull(
                    deviceId, startOfDay, endOfDay);

            List<UsageSession> overlapSessions = usageSessionRepository.findByDeviceIdAndStartTimeBeforeAndEndTimeAfter(
                    deviceId, startOfDay, startOfDay);
            sessions.addAll(overlapSessions);
        } else {
            sessions = usageSessionRepository.findByDeviceUserAndStartTimeBetweenAndEndTimeIsNotNull(
                    currentUser, startOfDay, endOfDay);

            List<UsageSession> overlapSessions = usageSessionRepository.findByDeviceUserAndStartTimeBeforeAndEndTimeAfter(
                    currentUser, startOfDay, startOfDay);
            sessions.addAll(overlapSessions);
        }

        List<AdjustedSessionDto> adjustedSessions = sessions.stream()
                .map(session -> calculateSessionTimeInDay(session, startOfDay, endOfDay))
                .collect(Collectors.toList());

        Map<LocalDateTime, Set<String>> deviceTimeMap = new TreeMap<>();
        for (AdjustedSessionDto session : adjustedSessions) {
            LocalDateTime current = session.getStartTime();
            while (current.isBefore(session.getEndTime())) {
                Set<String> activeDevices = deviceTimeMap.getOrDefault(current, new HashSet<>());
                activeDevices.add(session.getDeviceId());
                deviceTimeMap.put(current, activeDevices);
                current = current.plusMinutes(1);
            }
        }

        long totalNonOverlappingMinutes = deviceTimeMap.values().size();
        long totalUsageSeconds = totalNonOverlappingMinutes * 60;

        Map<String, Long> appUsageTime = calculateAppUsage(adjustedSessions);
        Map<String, Long> categoryUsageTime = calculateCategoryUsage(adjustedSessions);
        Map<String, Long> deviceUsageTime = calculateDeviceUsage(adjustedSessions);
        long productiveTimeSeconds = calculateProductiveTime(adjustedSessions);

        return DailyUsageStatsDto.builder()
                .date(date)
                .totalUsageSeconds(totalUsageSeconds)
                .applicationUsage(appUsageTime)
                .categoryUsage(categoryUsageTime)
                .deviceUsage(deviceUsageTime)
                .productiveTimeSeconds(productiveTimeSeconds)
                .nonProductiveTimeSeconds(totalUsageSeconds - productiveTimeSeconds)
                .build();
    }

    private AdjustedSessionDto calculateSessionTimeInDay(UsageSession session, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        AdjustedSessionDto adjusted = new AdjustedSessionDto();
        adjusted.setDeviceId(session.getDevice().getId().toString());
        adjusted.setApplication(session.getApplication());

        LocalDateTime sessionStart = session.getStartTime().isBefore(startOfDay) ?
                startOfDay : session.getStartTime();
        LocalDateTime sessionEnd = session.getEndTime().isAfter(endOfDay) ?
                endOfDay : session.getEndTime();

        adjusted.setStartTime(sessionStart);
        adjusted.setEndTime(sessionEnd);
        adjusted.setDurationSeconds(ChronoUnit.SECONDS.between(sessionStart, sessionEnd));
        return adjusted;
    }

    private Map<String, Long> calculateAppUsage(List<AdjustedSessionDto> sessions) {
        return sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getApplication().getName(),
                        Collectors.summingLong(AdjustedSessionDto::getDurationSeconds)
                ));
    }

    private Map<String, Long> calculateCategoryUsage(List<AdjustedSessionDto> sessions) {
        return sessions.stream()
                .filter(s -> s.getApplication().getCategory() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getApplication().getCategory().getName(),
                        Collectors.summingLong(AdjustedSessionDto::getDurationSeconds)
                ));
    }

    private Map<String, Long> calculateDeviceUsage(List<AdjustedSessionDto> sessions) {
        return sessions.stream()
                .collect(Collectors.groupingBy(
                        AdjustedSessionDto::getDeviceId,
                        Collectors.summingLong(AdjustedSessionDto::getDurationSeconds)
                ));
    }

    private long calculateProductiveTime(List<AdjustedSessionDto> sessions) {
        return sessions.stream()
                .filter(s -> s.getApplication().isProductive())
                .mapToLong(AdjustedSessionDto::getDurationSeconds)
                .sum();
    }

    @Override
    public MonthlyUsageStatsDto getMonthlyUsageStats(YearMonth month, Long deviceId) {
        User currentUser = userService.getCurrentUser();
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        Map<Integer, DailyUsageStatsDto> dailyStats = new HashMap<>();
        Map<Integer, Long> dailyUsageTrend = new HashMap<>();

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate currentDate = month.atDay(day);
            DailyUsageStatsDto stats = getDailyUsageStats(currentDate, deviceId);
            dailyStats.put(day, stats);
            dailyUsageTrend.put(day, stats.getTotalUsageSeconds());
        }

        long totalMonthlyUsageSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        Map<String, Long> appUsageTime = new HashMap<>();
        dailyStats.values().forEach(day -> {
            day.getApplicationUsage().forEach((app, time) -> {
                appUsageTime.merge(app, time, Long::sum);
            });
        });

        List<AppUsageDto> topApps = appUsageTime.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new AppUsageDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return MonthlyUsageStatsDto.builder()
                .month(month)
                .startDate(startDate)
                .endDate(endDate)
                .dailyStats(dailyStats)
                .dailyUsageTrend(dailyUsageTrend)
                .totalUsageSeconds(totalMonthlyUsageSeconds)
                .productiveTimeSeconds(productiveTimeSeconds)
                .nonProductiveTimeSeconds(totalMonthlyUsageSeconds - productiveTimeSeconds)
                .topApplications(topApps)
                .build();
    }

    @Override
    public YearlyUsageStatsDto getYearlyUsageStats(Year year, Long deviceId) {
        User currentUser = userService.getCurrentUser();
        Map<Month, MonthlyUsageStatsDto> monthlyStats = new HashMap<>();
        Map<Month, Long> monthlyUsageTrend = new HashMap<>();

        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year.getValue(), month);
            MonthlyUsageStatsDto stats = getMonthlyUsageStats(yearMonth, deviceId);
            monthlyStats.put(month, stats);
            monthlyUsageTrend.put(month, stats.getTotalUsageSeconds());
        }

        long totalYearlyUsageSeconds = monthlyStats.values().stream()
                .mapToLong(MonthlyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = monthlyStats.values().stream()
                .mapToLong(MonthlyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        Map<String, Long> appUsageTime = new HashMap<>();
        monthlyStats.values().forEach(month -> {
            month.getTopApplications().forEach(app -> {
                appUsageTime.merge(app.getName(), app.getDurationSeconds(), Long::sum);
            });
        });

        List<AppUsageDto> topApps = appUsageTime.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new AppUsageDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return YearlyUsageStatsDto.builder()
                .year(year)
                .monthlyStats(monthlyStats)
                .monthlyUsageTrend(monthlyUsageTrend)
                .totalUsageSeconds(totalYearlyUsageSeconds)
                .productiveTimeSeconds(productiveTimeSeconds)
                .nonProductiveTimeSeconds(totalYearlyUsageSeconds - productiveTimeSeconds)
                .topApplications(topApps)
                .build();
    }
}