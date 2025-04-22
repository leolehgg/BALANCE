package com.wellbeing.deviceusage.service.sync;

import com.wellbeing.deviceusage.dto.sync.SyncRequest;
import com.wellbeing.deviceusage.dto.sync.SyncResponse;
import com.wellbeing.deviceusage.dto.sync.UsageSessionSyncDto;
import com.wellbeing.deviceusage.dto.usage.UsageSessionDto;
import com.wellbeing.deviceusage.exception.ResourceNotFoundException;
import com.wellbeing.deviceusage.model.Application;
import com.wellbeing.deviceusage.model.Category;
import com.wellbeing.deviceusage.model.Device;
import com.wellbeing.deviceusage.model.UsageSession;
import com.wellbeing.deviceusage.model.User;
import com.wellbeing.deviceusage.repository.ApplicationRepository;
import com.wellbeing.deviceusage.repository.CategoryRepository;
import com.wellbeing.deviceusage.repository.DeviceRepository;
import com.wellbeing.deviceusage.repository.UsageSessionRepository;
import com.wellbeing.deviceusage.service.device.DeviceService;
import com.wellbeing.deviceusage.service.user.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SyncServiceImpl implements SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncServiceImpl.class);

    @Autowired
    private UsageSessionRepository usageSessionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public SyncResponse synchronizeData(SyncRequest request) {
        User currentUser = userService.getCurrentUser();

        Device device = deviceRepository.findByIdAndUser(request.getDeviceId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found or not authorized"));

        List<UsageSessionDto> processedSessions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (request.getSessions() != null) {
            for (UsageSessionSyncDto sessionDto : request.getSessions()) {
                try {
                    UsageSession session = processSessionSync(sessionDto, device);
                    processedSessions.add(toUsageSessionDto(session));
                } catch (Exception e) {
                    logger.error("Error processing session sync: {}", e.getMessage());
                    errors.add("Error processing session: " + sessionDto.getClientId() + " - " + e.getMessage());
                }
            }
        }

        deviceService.updateLastSync(device.getId());

        LocalDateTime lastClientSync = request.getLastSyncTimestamp() != null
                ? request.getLastSyncTimestamp()
                : LocalDateTime.now().minusDays(7);

        List<UsageSession> sessionsToSync = usageSessionRepository.findByDeviceUserAndUpdatedAtAfterAndDeviceIdNot(
                currentUser, lastClientSync, device.getId());

        List<UsageSessionDto> syncSessions = sessionsToSync.stream()
                .map(this::toUsageSessionDto)
                .collect(Collectors.toList());

        return SyncResponse.builder()
                .timestamp(LocalDateTime.now())
                .processedSessions(processedSessions)
                .syncSessions(syncSessions)
                .errors(errors)
                .build();
    }

    private UsageSession processSessionSync(UsageSessionSyncDto syncDto, Device device) {
        Optional<UsageSession> existingSession = usageSessionRepository.findByClientIdAndDevice(
                syncDto.getClientId(), device);

        if (existingSession.isPresent()) {
            UsageSession session = existingSession.get();
            if (syncDto.getVersion() > session.getVersion()) {
                session.setStartTime(syncDto.getStartTime());
                session.setEndTime(syncDto.getEndTime());
                session.setDurationSeconds(syncDto.getDurationSeconds());
                session.setVersion(syncDto.getVersion());
                session.setSynchronized_(true);
                return usageSessionRepository.save(session);
            }
            return session;
        } else {
            Application application = applicationRepository.findByPackageName(syncDto.getPackageName())
                    .orElseGet(() -> {
                        Application newApp = Application.builder()
                                .name(syncDto.getApplicationName())
                                .packageName(syncDto.getPackageName())
                                .productive(isProductiveApp(syncDto.getPackageName()))
                                .category(assignCategory(syncDto.getPackageName()))
                                .build();
                        return applicationRepository.save(newApp);
                    });

            UsageSession newSession = UsageSession.builder()
                    .clientId(syncDto.getClientId())
                    .device(device)
                    .application(application)
                    .startTime(syncDto.getStartTime())
                    .endTime(syncDto.getEndTime())
                    .durationSeconds(syncDto.getDurationSeconds())
                    .version(syncDto.getVersion())
                    .synchronized_(true)
                    .build();

            return usageSessionRepository.save(newSession);
        }
    }

    private Category assignCategory(String packageName) {
        if (packageName.contains("slack") || packageName.contains("gmail") || packageName.contains("notion")) {
            return categoryRepository.findByName("Productivity")
                    .orElseThrow(() -> new ResourceNotFoundException("Category 'Productivity' not found"));
        } else if (packageName.contains("whatsapp") || packageName.contains("twitter") || packageName.contains("instagram")) {
            return categoryRepository.findByName("Social Media")
                    .orElseThrow(() -> new ResourceNotFoundException("Category 'Social Media' not found"));
        } else if (packageName.contains("netflix") || packageName.contains("youtube")) {
            return categoryRepository.findByName("Entertainment")
                    .orElseThrow(() -> new ResourceNotFoundException("Category 'Entertainment' not found"));
        } else if (packageName.contains("chrome")) {
            return categoryRepository.findByName("Utilities")
                    .orElseThrow(() -> new ResourceNotFoundException("Category 'Utilities' not found"));
        }
        return null; // Si no se encuentra una categoría adecuada, se deja como null
    }

    private boolean isProductiveApp(String packageName) {
        return packageName.contains("slack") || packageName.contains("gmail") || packageName.contains("notion");
    }

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
}