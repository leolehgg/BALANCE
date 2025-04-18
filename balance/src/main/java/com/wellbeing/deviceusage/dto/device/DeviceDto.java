package com.wellbeing.deviceusage.dto.device;

import com.wellbeing.deviceusage.model.DeviceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDto {
    private Long id;
    private String deviceUuid;
    private String name;
    private DeviceType type;
    private String platform;
    private String version;
    private LocalDateTime lastSync;
    private boolean active;
}