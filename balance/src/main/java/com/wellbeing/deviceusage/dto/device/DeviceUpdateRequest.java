package com.wellbeing.deviceusage.dto.device;

import lombok.Data;

@Data
public class DeviceUpdateRequest {
    private String name;
    private String version;
    private Boolean active;
}