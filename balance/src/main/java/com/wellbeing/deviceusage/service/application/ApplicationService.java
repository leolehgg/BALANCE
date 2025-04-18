package com.wellbeing.deviceusage.service.application;

import com.wellbeing.deviceusage.dto.application.ApplicationDto;
import com.wellbeing.deviceusage.dto.application.ApplicationRequest;

import java.util.List;

public interface ApplicationService {
    List<ApplicationDto> getAllApplications();

    ApplicationDto getApplicationById(Long id);

    ApplicationDto updateApplication(Long id, ApplicationRequest request);
}