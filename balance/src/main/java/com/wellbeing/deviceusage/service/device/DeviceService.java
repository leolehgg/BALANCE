package com.wellbeing.deviceusage.service.device;

import com.wellbeing.deviceusage.dto.device.DeviceDto;
import com.wellbeing.deviceusage.dto.device.DeviceRegistrationRequest;
import com.wellbeing.deviceusage.dto.device.DeviceUpdateRequest;

import java.util.List;

public interface DeviceService {
    DeviceDto registerDevice(DeviceRegistrationRequest request);

    List<DeviceDto> getCurrentUserDevices();

    DeviceDto getDeviceById(Long id);

    DeviceDto updateDevice(Long id, DeviceUpdateRequest request);

    void deleteDevice(Long id);

    void updateLastSync(Long deviceId);
}