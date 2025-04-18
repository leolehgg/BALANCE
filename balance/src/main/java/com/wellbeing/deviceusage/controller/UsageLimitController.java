package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.limit.UsageLimitDto;
import com.wellbeing.deviceusage.dto.limit.UsageLimitRequest;
import com.wellbeing.deviceusage.service.limit.UsageLimitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/limits")
@PreAuthorize("hasRole('USER')")
public class UsageLimitController {
    @Autowired
    private UsageLimitService usageLimitService;

    @PostMapping
    public ResponseEntity<UsageLimitDto> createLimit(@Valid @RequestBody UsageLimitRequest request) {
        UsageLimitDto limitDto = usageLimitService.createLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(limitDto);
    }

    @GetMapping
    public ResponseEntity<List<UsageLimitDto>> getUserLimits() {
        List<UsageLimitDto> limits = usageLimitService.getCurrentUserLimits();
        return ResponseEntity.ok(limits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsageLimitDto> getLimit(@PathVariable Long id) {
        UsageLimitDto limitDto = usageLimitService.getLimitById(id);
        return ResponseEntity.ok(limitDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsageLimitDto> updateLimit(
            @PathVariable Long id,
            @Valid @RequestBody UsageLimitRequest request) {
        UsageLimitDto limitDto = usageLimitService.updateLimit(id, request);
        return ResponseEntity.ok(limitDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLimit(@PathVariable Long id) {
        usageLimitService.deleteLimit(id);
        return ResponseEntity.noContent().build();
    }
}