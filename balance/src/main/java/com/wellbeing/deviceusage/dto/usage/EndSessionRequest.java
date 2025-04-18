package com.wellbeing.deviceusage.dto.usage;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndSessionRequest {
    @NotNull
    private LocalDateTime endTime;
}