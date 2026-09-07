package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.ReviewApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Admin operations for reviewing academy applications.
 */
public interface AcademyAdminApplicationService {

    Page<IndividualApplicationResponse> listIndividuals(String status, Pageable pageable);

    Page<InstitutionApplicationResponse> listInstitutions(String status, Pageable pageable);

    IndividualApplicationResponse reviewIndividual(UUID applicationId, ReviewApplicationRequest request);

    InstitutionApplicationResponse reviewInstitution(UUID applicationId, ReviewApplicationRequest request);
}
