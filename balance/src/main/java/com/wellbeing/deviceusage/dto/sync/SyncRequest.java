package com.wellbeing.deviceusage.dto.sync;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SyncRequest {

    @NotNull
    private Long deviceId;

    private LocalDateTime lastSyncTimestamp;

    private List<UsageSessionSyncDto> sessions;
}