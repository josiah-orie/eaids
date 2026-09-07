package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.EnrollApplicantRequest;
import com.feadev.eaids.academy.dto.response.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Admin operations for enrolling approved applicants into training schedules.
 */
public interface AcademyScheduleEnrollmentService {

    EnrollmentResponse enroll(UUID scheduleId, EnrollApplicantRequest request);

    void unenroll(UUID scheduleId, UUID enrollmentId);

    List<EnrollmentResponse> listEnrollments(UUID scheduleId);
}
