package com.wellbeing.deviceusage.repository;

import com.wellbeing.deviceusage.model.UsageLimit;
import com.wellbeing.deviceusage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsageLimitRepository extends JpaRepository<UsageLimit, Long> {
    List<UsageLimit> findByUser(User user);

    Optional<UsageLimit> findByIdAndUser(Long id, User user);

    List<UsageLimit> findByUserAndCategoryId(User user, Long categoryId);

    List<UsageLimit> findByUserAndApplicationId(User user, Long applicationId);
}