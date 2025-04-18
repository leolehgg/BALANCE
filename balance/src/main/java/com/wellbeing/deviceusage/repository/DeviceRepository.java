package com.wellbeing.deviceusage.repository;

import com.wellbeing.deviceusage.model.Device;
import com.wellbeing.deviceusage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserAndActiveTrue(User user);

    Optional<Device> findByIdAndUser(Long id, User user);

    boolean existsByDeviceUuidAndUser(String deviceUuid, User user);
}