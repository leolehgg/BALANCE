package com.wellbeing.deviceusage.service.limit;

import com.wellbeing.deviceusage.dto.limit.UsageLimitDto;
import com.wellbeing.deviceusage.dto.limit.UsageLimitRequest;

import java.util.List;

public interface UsageLimitService {
    UsageLimitDto createLimit(UsageLimitRequest request);

    List<UsageLimitDto> getCurrentUserLimits();

    UsageLimitDto getLimitById(Long id);

    UsageLimitDto updateLimit(Long id, UsageLimitRequest request);

    void deleteLimit(Long id);
}