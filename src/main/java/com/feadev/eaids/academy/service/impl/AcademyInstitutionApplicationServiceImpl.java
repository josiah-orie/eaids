package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.CreateInstitutionApplicationRequest;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.service.AcademyInstitutionApplicationService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyInstitutionApplicationServiceImpl implements AcademyInstitutionApplicationService {

    private final AcademyInstitutionApplicationRepository repository;

    @Override
    public InstitutionApplicationResponse submit(CreateInstitutionApplicationRequest request) {
        if (repository.existsByContactEmail(request.getContactEmail())) {
            throw new DuplicateResourceException(
                    "An institution application with this contact email already exists");
        }

        AcademyInstitutionApplication application = AcademyInstitutionApplication.builder()
                .institutionType(request.getInstitutionType())
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress().toEmbeddable())
                .status(ApplicationStatus.PENDING)
                .build();

        AcademyInstitutionApplication saved = repository.save(application);
        log.info("Institution academy application submitted: id={}, contactEmail={}",
                saved.getId(), saved.getContactEmail());

        return AcademyMapper.toResponse(saved);
    }
}
