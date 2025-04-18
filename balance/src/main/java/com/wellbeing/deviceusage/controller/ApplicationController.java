package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.application.ApplicationDto;
import com.wellbeing.deviceusage.dto.application.ApplicationRequest;
import com.wellbeing.deviceusage.service.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@PreAuthorize("hasRole('USER')")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        List<ApplicationDto> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable Long id) {
        ApplicationDto applicationDto = applicationService.getApplicationById(id);
        return ResponseEntity.ok(applicationDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDto> updateApplication(
            @PathVariable Long id,
            @RequestBody ApplicationRequest request) {
        ApplicationDto applicationDto = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(applicationDto);
    }
}