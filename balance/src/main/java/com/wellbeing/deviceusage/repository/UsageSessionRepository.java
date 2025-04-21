package com.wellbeing.deviceusage.repository;

import com.wellbeing.deviceusage.model.Device;
import com.wellbeing.deviceusage.model.UsageSession;
import com.wellbeing.deviceusage.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageSessionRepository extends JpaRepository<UsageSession, Long> {
    Page<UsageSession> findByDeviceIdAndStartTimeBetween(
            Long deviceId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<UsageSession> findByDeviceUserAndStartTimeBetween(
            User user, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    List<UsageSession> findByDeviceIdAndStartTimeBetweenAndEndTimeIsNotNull(
            Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    List<UsageSession> findByDeviceUserAndStartTimeBetweenAndEndTimeIsNotNull(
            User user, LocalDateTime startTime, LocalDateTime endTime);

    Optional<UsageSession> findByIdAndDeviceUser(Long id, User user);

    Optional<UsageSession> findByClientIdAndDevice(String clientId, Device device);

    List<UsageSession> findByDeviceUserAndUpdatedAtAfterAndDeviceIdNot(
            User user, LocalDateTime afterTime, Long deviceId);

    // Agregar a UsageSessionRepository
    List<UsageSession> findByDeviceIdAndStartTimeBeforeAndEndTimeAfter(
            Long deviceId, LocalDateTime date, LocalDateTime sameDate);

    List<UsageSession> findByDeviceUserAndStartTimeBeforeAndEndTimeAfter(
            User user, LocalDateTime date, LocalDateTime sameDate);
}