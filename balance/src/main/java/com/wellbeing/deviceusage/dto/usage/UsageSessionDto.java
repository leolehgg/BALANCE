package com.wellbeing.deviceusage.dto.usage;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsageSessionDto {
    private Long id;
    private String clientId;
    private String applicationName;
    private String packageName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
}