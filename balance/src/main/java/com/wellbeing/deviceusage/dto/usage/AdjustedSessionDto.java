package com.wellbeing.deviceusage.dto.usage;

import com.wellbeing.deviceusage.model.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustedSessionDto {
    private String deviceId;
    private Application application;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
}