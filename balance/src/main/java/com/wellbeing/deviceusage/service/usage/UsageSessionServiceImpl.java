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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // Validate device belongs to current user
        User currentUser = userService.getCurrentUser();
        Device device = deviceRepository.findByIdAndUser(request.getDeviceId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

        // Get or create application
        Application application = applicationRepository.findByPackageName(request.getPackageName())
                .orElseGet(() -> {
                    Application newApp = Application.builder()
                            .name(request.getApplicationName())
                            .packageName(request.getPackageName())
                            .productive(false) // Default value, can be updated later
                            .build();
                    return applicationRepository.save(newApp);
                });

        // Create new session
        UsageSession session = UsageSession.builder()
                .device(device)
                .application(application)
                .startTime(LocalDateTime.now())
                .synchronized_(false)
                .build();

        UsageSession savedSession = usageSessionRepository.save(session);

        return modelMapper.map(savedSession, UsageSessionDto.class);
    }

    @Override
    public UsageSessionDto endSession(Long sessionId, EndSessionRequest request) {
        // Validate session belongs to current user
        User currentUser = userService.getCurrentUser();
        UsageSession session = usageSessionRepository.findByIdAndDeviceUser(sessionId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found or not authorized"));

        // Set end time and calculate duration
        LocalDateTime endTime = request.getEndTime();
        session.setEndTime(endTime);

        long durationSeconds = ChronoUnit.SECONDS.between(session.getStartTime(), endTime);
        session.setDurationSeconds(durationSeconds);

        UsageSession updatedSession = usageSessionRepository.save(session);

        return modelMapper.map(updatedSession, UsageSessionDto.class);
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
            // Validate device belongs to user
            deviceRepository.findByIdAndUser(deviceId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

            sessions = usageSessionRepository.findByDeviceIdAndStartTimeBetween(
                    deviceId, startDateTime, endDateTime, pageable);
        } else {
            sessions = usageSessionRepository.findByDeviceUserAndStartTimeBetween(
                    currentUser, startDateTime, endDateTime, pageable);
        }

        return sessions.map(session -> modelMapper.map(session, UsageSessionDto.class));
    }

    @Override
    public DailyUsageStatsDto getDailyUsageStats(LocalDate date, Long deviceId) {
        User currentUser = userService.getCurrentUser();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<UsageSession> sessions;
        if (deviceId != null) {
            // Validate device belongs to user
            deviceRepository.findByIdAndUser(deviceId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

            sessions = usageSessionRepository.findByDeviceIdAndStartTimeBetweenAndEndTimeIsNotNull(
                    deviceId, startOfDay, endOfDay);
        } else {
            sessions = usageSessionRepository.findByDeviceUserAndStartTimeBetweenAndEndTimeIsNotNull(
                    currentUser, startOfDay, endOfDay);
        }

        // Calculate statistics
        long totalUsageSeconds = sessions.stream()
                .mapToLong(UsageSession::getDurationSeconds)
                .sum();

        // Group by application
        Map<String, Long> appUsageTime = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getApplication().getName(),
                        Collectors.summingLong(UsageSession::getDurationSeconds)
                ));

        // Group by category
        Map<String, Long> categoryUsageTime = sessions.stream()
                .filter(s -> s.getApplication().getCategory() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getApplication().getCategory().getName(),
                        Collectors.summingLong(UsageSession::getDurationSeconds)
                ));

        // Group by device
        Map<String, Long> deviceUsageTime = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDevice().getName(),
                        Collectors.summingLong(UsageSession::getDurationSeconds)
                ));

        // Productive vs non-productive time
        long productiveTimeSeconds = sessions.stream()
                .filter(s -> s.getApplication().isProductive())
                .mapToLong(UsageSession::getDurationSeconds)
                .sum();

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

    @Override
    public WeeklyUsageStatsDto getWeeklyUsageStats(LocalDate startOfWeek, Long deviceId) {
        User currentUser = userService.getCurrentUser();

        Map<DayOfWeek, DailyUsageStatsDto> dailyStats = new HashMap<>();

        // Get stats for each day of the week
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startOfWeek.plusDays(i);
            DailyUsageStatsDto stats = getDailyUsageStats(currentDate, deviceId);
            dailyStats.put(currentDate.getDayOfWeek(), stats);
        }

        // Calculate weekly totals
        long totalWeeklyUsageSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        // Merge application usage
        Map<String, Long> appUsageTime = new HashMap<>();
        dailyStats.values().forEach(day -> {
            day.getApplicationUsage().forEach((app, time) -> {
                appUsageTime.merge(app, time, Long::sum);
            });
        });

        // Get top 5 apps
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
}