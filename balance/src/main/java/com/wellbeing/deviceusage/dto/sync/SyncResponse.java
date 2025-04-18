package com.wellbeing.deviceusage.dto.sync;

import com.wellbeing.deviceusage.dto.usage.UsageSessionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncResponse {
    private LocalDateTime timestamp;
    private List<UsageSessionDto> processedSessions;
    private List<UsageSessionDto> syncSessions;
    private List<String> errors;
}