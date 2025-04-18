package com.wellbeing.deviceusage.repository;

import com.wellbeing.deviceusage.model.ERole;
import com.wellbeing.deviceusage.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}