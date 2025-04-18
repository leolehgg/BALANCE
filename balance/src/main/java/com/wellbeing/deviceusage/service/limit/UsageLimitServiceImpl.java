package com.wellbeing.deviceusage.service.limit;

import com.wellbeing.deviceusage.dto.limit.UsageLimitDto;
import com.wellbeing.deviceusage.dto.limit.UsageLimitRequest;
import com.wellbeing.deviceusage.exception.ResourceNotFoundException;
import com.wellbeing.deviceusage.model.Application;
import com.wellbeing.deviceusage.model.Category;
import com.wellbeing.deviceusage.model.UsageLimit;
import com.wellbeing.deviceusage.model.User;
import com.wellbeing.deviceusage.repository.ApplicationRepository;
import com.wellbeing.deviceusage.repository.CategoryRepository;
import com.wellbeing.deviceusage.repository.UsageLimitRepository;
import com.wellbeing.deviceusage.service.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsageLimitServiceImpl implements UsageLimitService {
    @Autowired
    private UsageLimitRepository usageLimitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UsageLimitDto createLimit(UsageLimitRequest request) {
        // Either category or application must be provided, but not both
        if ((request.getCategoryId() == null && request.getApplicationId() == null) ||
                (request.getCategoryId() != null && request.getApplicationId() != null)) {
            throw new IllegalArgumentException("Either categoryId or applicationId must be provided, but not both");
        }

        User currentUser = userService.getCurrentUser();

        UsageLimit limit = new UsageLimit();
        limit.setUser(currentUser);
        limit.setDailyLimitMinutes(request.getDailyLimitMinutes());
        limit.setDaysOfWeek(request.getDaysOfWeek());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

            limit.setCategory(category);
        } else {
            Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + request.getApplicationId()));

            limit.setApplication(application);
        }

        UsageLimit savedLimit = usageLimitRepository.save(limit);
        return convertToDto(savedLimit);
    }

    @Override
    public List<UsageLimitDto> getCurrentUserLimits() {
        User currentUser = userService.getCurrentUser();
        List<UsageLimit> limits = usageLimitRepository.findByUser(currentUser);

        return limits.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UsageLimitDto getLimitById(Long id) {
        User currentUser = userService.getCurrentUser();
        UsageLimit limit = usageLimitRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usage limit not found with id: " + id));

        return convertToDto(limit);
    }

    @Override
    public UsageLimitDto updateLimit(Long id, UsageLimitRequest request) {
        User currentUser = userService.getCurrentUser();
        UsageLimit limit = usageLimitRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usage limit not found with id: " + id));

        // Update limit fields
        limit.setDailyLimitMinutes(request.getDailyLimitMinutes());

        if (request.getDaysOfWeek() != null) {
            limit.setDaysOfWeek(request.getDaysOfWeek());
        }

        // Cannot change from category to application or vice versa
        // Only update if the type matches the existing limit
        if (request.getCategoryId() != null && limit.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

            limit.setCategory(category);
        } else if (request.getApplicationId() != null && limit.getApplication() != null) {
            Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + request.getApplicationId()));

            limit.setApplication(application);
        }

        UsageLimit updatedLimit = usageLimitRepository.save(limit);
        return convertToDto(updatedLimit);
    }

    @Override
    public void deleteLimit(Long id) {
        User currentUser = userService.getCurrentUser();
        UsageLimit limit = usageLimitRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usage limit not found with id: " + id));

        usageLimitRepository.delete(limit);
    }

    private UsageLimitDto convertToDto(UsageLimit limit) {
        UsageLimitDto dto = modelMapper.map(limit, UsageLimitDto.class);

        if (limit.getCategory() != null) {
            dto.setCategoryId(limit.getCategory().getId());
            dto.setCategoryName(limit.getCategory().getName());
        }

        if (limit.getApplication() != null) {
            dto.setApplicationId(limit.getApplication().getId());
            dto.setApplicationName(limit.getApplication().getName());
        }

        return dto;
    }
}