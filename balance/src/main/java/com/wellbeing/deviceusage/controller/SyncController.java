package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.sync.SyncRequest;
import com.wellbeing.deviceusage.dto.sync.SyncResponse;
import com.wellbeing.deviceusage.service.sync.SyncService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@PreAuthorize("hasRole('USER')")
public class SyncController {
    @Autowired
    private SyncService syncService;

    @PostMapping
    public ResponseEntity<SyncResponse> synchronizeData(@Valid @RequestBody SyncRequest request) {
        SyncResponse response = syncService.synchronizeData(request);
        return ResponseEntity.ok(response);
    }
}