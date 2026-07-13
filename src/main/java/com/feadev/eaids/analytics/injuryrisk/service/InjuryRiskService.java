package com.feadev.eaids.analytics.injuryrisk.service;

import com.feadev.eaids.analytics.injuryrisk.dto.InjuryRiskResponse;

import java.util.List;
import java.util.UUID;

public interface InjuryRiskService {

    InjuryRiskResponse assessEnrollment(UUID enrollmentId);

    List<InjuryRiskResponse> batchAssessCamp(UUID campId);
}
