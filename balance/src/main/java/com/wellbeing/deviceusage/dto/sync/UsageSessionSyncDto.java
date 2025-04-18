package com.wellbeing.deviceusage.dto.sync;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsageSessionSyncDto {
    private String clientId;
    private String packageName;
    private String applicationName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    private int version;
}