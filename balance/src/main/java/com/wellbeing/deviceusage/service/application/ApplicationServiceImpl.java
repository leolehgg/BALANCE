package com.wellbeing.deviceusage.service.application;

import com.wellbeing.deviceusage.dto.application.ApplicationDto;
import com.wellbeing.deviceusage.dto.application.ApplicationRequest;
import com.wellbeing.deviceusage.exception.ResourceNotFoundException;
import com.wellbeing.deviceusage.model.Application;
import com.wellbeing.deviceusage.model.Category;
import com.wellbeing.deviceusage.repository.ApplicationRepository;
import com.wellbeing.deviceusage.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findAll();
        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationDto getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        return convertToDto(application);
    }

    @Override
    public ApplicationDto updateApplication(Long id, ApplicationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

            application.setCategory(category);
        }

        if (request.getProductive() != null) {
            application.setProductive(request.getProductive());
        }

        Application updatedApplication = applicationRepository.save(application);
        return convertToDto(updatedApplication);
    }

    private ApplicationDto convertToDto(Application application) {
        ApplicationDto dto = modelMapper.map(application, ApplicationDto.class);

        if (application.getCategory() != null) {
            dto.setCategoryId(application.getCategory().getId());
            dto.setCategoryName(application.getCategory().getName());
        }

        return dto;
    }
}