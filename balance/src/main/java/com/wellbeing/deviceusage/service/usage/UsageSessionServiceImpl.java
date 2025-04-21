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

            // También incluir sesiones que comenzaron antes pero terminaron durante este día
            List<UsageSession> overlapSessions = usageSessionRepository.findByDeviceIdAndStartTimeBeforeAndEndTimeAfter(
                    deviceId, startOfDay, startOfDay);
            sessions.addAll(overlapSessions);
        } else {
            sessions = usageSessionRepository.findByDeviceUserAndStartTimeBetweenAndEndTimeIsNotNull(
                    currentUser, startOfDay, endOfDay);

            // También incluir sesiones que comenzaron antes pero terminaron durante este día
            List<UsageSession> overlapSessions = usageSessionRepository.findByDeviceUserAndStartTimeBeforeAndEndTimeAfter(
                    currentUser, startOfDay, startOfDay);
            sessions.addAll(overlapSessions);
        }

        // Ajustar el tiempo de cada sesión para que solo cuente el tiempo dentro del día actual
        List<AdjustedSessionDto> adjustedSessions = sessions.stream()
                .map(session -> calculateSessionTimeInDay(session, startOfDay, endOfDay))
                .collect(Collectors.toList());

        // Calcular tiempo total sin superposiciones entre dispositivos
        Map<LocalDateTime, Set<String>> deviceTimeMap = new TreeMap<>();

        // Crear un mapa de cada minuto del día y qué dispositivos estaban activos
        for (AdjustedSessionDto session : adjustedSessions) {
            LocalDateTime current = session.getStartTime();
            while (current.isBefore(session.getEndTime())) {
                Set<String> activeDevices = deviceTimeMap.getOrDefault(current, new HashSet<>());
                activeDevices.add(session.getDeviceId());
                deviceTimeMap.put(current, activeDevices);
                current = current.plusMinutes(1);
            }
        }

        // Calcular el tiempo total sin superposiciones (en minutos)
        long totalNonOverlappingMinutes = deviceTimeMap.values().size();
        long totalUsageSeconds = totalNonOverlappingMinutes * 60;

        // Calcular estadísticas por app y categoría (ajustando por tiempo real en el día)
        Map<String, Long> appUsageTime = calculateAppUsage(adjustedSessions);
        Map<String, Long> categoryUsageTime = calculateCategoryUsage(adjustedSessions);
        Map<String, Long> deviceUsageTime = calculateDeviceUsage(adjustedSessions);

        // Productive vs non-productive time
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

        // Ajustar tiempos para que estén dentro del día
        LocalDateTime sessionStart = session.getStartTime().isBefore(startOfDay) ?
                startOfDay : session.getStartTime();
        LocalDateTime sessionEnd = session.getEndTime().isAfter(endOfDay) ?
                endOfDay : session.getEndTime();

        adjusted.setStartTime(sessionStart);
        adjusted.setEndTime(sessionEnd);

        // Calcular la duración ajustada
        adjusted.setDurationSeconds(ChronoUnit.SECONDS.between(sessionStart, sessionEnd));

        return adjusted;
    }

    // Métodos para calcular uso por aplicación, categoría y dispositivo
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

        // Obtener estadísticas para cada día del mes
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate currentDate = month.atDay(day);
            DailyUsageStatsDto stats = getDailyUsageStats(currentDate, deviceId);

            dailyStats.put(day, stats);
            dailyUsageTrend.put(day, stats.getTotalUsageSeconds());
        }

        // Calcular totales mensuales
        long totalMonthlyUsageSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = dailyStats.values().stream()
                .mapToLong(DailyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        // Fusionar uso de aplicaciones
        Map<String, Long> appUsageTime = new HashMap<>();
        dailyStats.values().forEach(day -> {
            day.getApplicationUsage().forEach((app, time) -> {
                appUsageTime.merge(app, time, Long::sum);
            });
        });

        // Obtener top 5 apps
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

        // Obtener estadísticas para cada mes del año
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year.getValue(), month);
            MonthlyUsageStatsDto stats = getMonthlyUsageStats(yearMonth, deviceId);

            monthlyStats.put(month, stats);
            monthlyUsageTrend.put(month, stats.getTotalUsageSeconds());
        }

        // Calcular totales anuales
        long totalYearlyUsageSeconds = monthlyStats.values().stream()
                .mapToLong(MonthlyUsageStatsDto::getTotalUsageSeconds)
                .sum();

        long productiveTimeSeconds = monthlyStats.values().stream()
                .mapToLong(MonthlyUsageStatsDto::getProductiveTimeSeconds)
                .sum();

        // Fusionar uso de aplicaciones de todos los meses
        Map<String, Long> appUsageTime = new HashMap<>();
        monthlyStats.values().forEach(month -> {
            month.getTopApplications().forEach(app -> {
                appUsageTime.merge(app.getName(), app.getDurationSeconds(), Long::sum);
            });
        });

        // Obtener top 5 apps del año
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