package com.wellbeing.deviceusage.dto.application;

import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private String name;
    private String packageName;
    private Long categoryId;
    private String categoryName;
    private boolean productive;
}