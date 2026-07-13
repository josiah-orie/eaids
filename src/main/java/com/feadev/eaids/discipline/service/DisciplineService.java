package com.feadev.eaids.discipline.service;

import com.feadev.eaids.discipline.dto.request.RecordInfractionRequest;
import com.feadev.eaids.discipline.dto.response.DisciplineResponse;
import com.feadev.eaids.discipline.dto.response.DisciplineSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface DisciplineService {

    DisciplineResponse recordInfraction(UUID campId, RecordInfractionRequest request, String recordedBy);

    List<DisciplineResponse> getInfractionsByEnrollment(UUID enrollmentId);

    DisciplineSummaryResponse getDisciplineSummary(UUID enrollmentId);
}
