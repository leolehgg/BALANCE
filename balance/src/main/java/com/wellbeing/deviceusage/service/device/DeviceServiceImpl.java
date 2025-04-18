package com.wellbeing.deviceusage.service.device;

import com.wellbeing.deviceusage.dto.device.DeviceDto;
import com.wellbeing.deviceusage.dto.device.DeviceRegistrationRequest;
import com.wellbeing.deviceusage.dto.device.DeviceUpdateRequest;
import com.wellbeing.deviceusage.exception.DeviceAlreadyExistsException;
import com.wellbeing.deviceusage.exception.ResourceNotFoundException;
import com.wellbeing.deviceusage.model.Device;
import com.wellbeing.deviceusage.model.User;
import com.wellbeing.deviceusage.repository.DeviceRepository;
import com.wellbeing.deviceusage.service.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public DeviceDto registerDevice(DeviceRegistrationRequest request) {
        User currentUser = userService.getCurrentUser();

        // Check if device UUID already exists for this user
        if (deviceRepository.existsByDeviceUuidAndUser(request.getDeviceUuid(), currentUser)) {
            throw new DeviceAlreadyExistsException("Device already registered for this user");
        }

        Device device = Device.builder()
                .deviceUuid(request.getDeviceUuid())
                .name(request.getName())
                .type(request.getType())
                .platform(request.getPlatform())
                .version(request.getVersion())
                .user(currentUser)
                .lastSync(LocalDateTime.now())
                .active(true)
                .build();

        Device savedDevice = deviceRepository.save(device);
        return modelMapper.map(savedDevice, DeviceDto.class);
    }

    @Override
    public List<DeviceDto> getCurrentUserDevices() {
        User currentUser = userService.getCurrentUser();
        List<Device> devices = deviceRepository.findByUserAndActiveTrue(currentUser);
        return devices.stream()
                .map(device -> modelMapper.map(device, DeviceDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDto getDeviceById(Long id) {
        User currentUser = userService.getCurrentUser();
        Device device = deviceRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        return modelMapper.map(device, DeviceDto.class);
    }

    @Override
    public DeviceDto updateDevice(Long id, DeviceUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        Device device = deviceRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        // Update device properties
        if (request.getName() != null) {
            device.setName(request.getName());
        }

        if (request.getVersion() != null) {
            device.setVersion(request.getVersion());
        }

        if (request.getActive() != null) {
            device.setActive(request.getActive());
        }

        // Update last sync time
        device.setLastSync(LocalDateTime.now());

        Device updatedDevice = deviceRepository.save(device);
        return modelMapper.map(updatedDevice, DeviceDto.class);
    }

    @Override
    public void deleteDevice(Long id) {
        User currentUser = userService.getCurrentUser();
        Device device = deviceRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        // Soft delete by marking as inactive
        device.setActive(false);
        deviceRepository.save(device);
    }

    @Override
    public void updateLastSync(Long deviceId) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setLastSync(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }
}