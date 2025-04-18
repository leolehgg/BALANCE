package com.wellbeing.deviceusage.dto.application;

import lombok.Data;

@Data
public class ApplicationRequest {
    private Long categoryId;
    private Boolean productive;
}