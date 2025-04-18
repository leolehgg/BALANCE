package com.wellbeing.deviceusage.repository;

import com.wellbeing.deviceusage.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByPackageName(String packageName);
}