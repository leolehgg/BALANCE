package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.usage.*;
import com.wellbeing.deviceusage.service.usage.UsageSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/usage")
@PreAuthorize("hasRole('USER')")
public class UsageController {
    @Autowired
    private UsageSessionService usageSessionService;

    @PostMapping("/sessions/start")
    public ResponseEntity<UsageSessionDto> startSession(@Valid @RequestBody StartSessionRequest request) {
        UsageSessionDto sessionDto = usageSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionDto);
    }

    @PostMapping("/sessions/{id}/end")
    public ResponseEntity<UsageSessionDto> endSession(
            @PathVariable Long id,
            @Valid @RequestBody EndSessionRequest request) {
        UsageSessionDto sessionDto = usageSessionService.endSession(id, request);
        return ResponseEntity.ok(sessionDto);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<UsageSessionDto>> getUserSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Page<UsageSessionDto> sessions = usageSessionService.getUserSessions(page, size, deviceId, startDate, endDate);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<DailyUsageStatsDto> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long deviceId) {

        DailyUsageStatsDto stats = usageSessionService.getDailyUsageStats(date, deviceId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<WeeklyUsageStatsDto> getWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfWeek,
            @RequestParam(required = false) Long deviceId) {

        WeeklyUsageStatsDto stats = usageSessionService.getWeeklyUsageStats(startOfWeek, deviceId);
        return ResponseEntity.ok(stats);
    }
}