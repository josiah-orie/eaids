package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.CreateIndividualApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.service.AcademyIndividualApplicationService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyIndividualApplicationServiceImpl implements AcademyIndividualApplicationService {

    private final AcademyIndividualApplicationRepository repository;

    @Override
    public IndividualApplicationResponse submit(CreateIndividualApplicationRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An individual application with this email already exists");
        }

        AcademyIndividualApplication application = AcademyIndividualApplication.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .email(request.getEmail())
                .phone(request.getPhone())
                .location(request.getLocation())
                .address(request.getAddress().toEmbeddable())
                .status(ApplicationStatus.PENDING)
                .build();

        AcademyIndividualApplication saved = repository.save(application);
        log.info("Individual academy application submitted: id={}, email={}",
                saved.getId(), saved.getEmail());

        return AcademyMapper.toResponse(saved);
    }
}
