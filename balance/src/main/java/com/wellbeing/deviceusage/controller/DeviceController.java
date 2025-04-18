package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.device.DeviceDto;
import com.wellbeing.deviceusage.dto.device.DeviceRegistrationRequest;
import com.wellbeing.deviceusage.dto.device.DeviceUpdateRequest;
import com.wellbeing.deviceusage.service.device.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasRole('USER')")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceDto> registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        DeviceDto deviceDto = deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceDto);
    }

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getUserDevices() {
        List<DeviceDto> devices = deviceService.getCurrentUserDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDevice(@PathVariable Long id) {
        DeviceDto deviceDto = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceUpdateRequest request) {
        DeviceDto deviceDto = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(deviceDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}