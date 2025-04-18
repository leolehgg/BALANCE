package com.wellbeing.deviceusage.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppUsageDto {
    private String name;
    private Long durationSeconds;
}