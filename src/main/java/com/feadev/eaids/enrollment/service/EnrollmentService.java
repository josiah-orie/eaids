package com.feadev.eaids.enrollment.service;

import com.feadev.eaids.enrollment.dto.response.EnrollmentResponse;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse enrollAthlete(UUID campId, UUID athleteUserId);

    List<EnrollmentResponse> getCampEnrollments(UUID campId);

    List<EnrollmentResponse> getCampEnrollmentsByStatus(UUID campId, EnrollmentStatus status);

    EnrollmentResponse updateEnrollmentStatus(UUID campId, UUID enrollmentId, EnrollmentStatus newStatus);

    List<EnrollmentResponse> getAthleteEnrollments(UUID athleteUserId);
}
