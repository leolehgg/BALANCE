package com.wellbeing.deviceusage.dto.device;

import com.wellbeing.deviceusage.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceRegistrationRequest {
    @NotBlank
    private String deviceUuid;

    @NotBlank
    private String name;

    @NotNull
    private DeviceType type;

    @NotBlank
    private String platform;

    @NotBlank
    private String version;
}