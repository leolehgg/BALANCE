package com.wellbeing.deviceusage.dto.usage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSessionRequest {
    @NotNull
    private Long deviceId;

    @NotBlank
    private String packageName;

    @NotBlank
    private String applicationName;
}