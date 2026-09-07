package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.CreateInstitutionApplicationRequest;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;

/**
 * Public onboarding operations for institution academy applicants.
 */
public interface AcademyInstitutionApplicationService {

    /**
     * Submits a new institution application in PENDING status.
     *
     * @param request the institution details
     * @return the persisted application
     */
    InstitutionApplicationResponse submit(CreateInstitutionApplicationRequest request);
}
