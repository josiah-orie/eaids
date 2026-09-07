package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.CreateIndividualApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;

/**
 * Public onboarding operations for individual academy applicants.
 */
public interface AcademyIndividualApplicationService {

    /**
     * Submits a new individual application in PENDING status.
     *
     * @param request the applicant details
     * @return the persisted application
     */
    IndividualApplicationResponse submit(CreateIndividualApplicationRequest request);
}
