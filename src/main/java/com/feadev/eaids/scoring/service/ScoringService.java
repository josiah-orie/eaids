package com.feadev.eaids.scoring.service;

import com.feadev.eaids.scoring.dto.request.CreateWeightProfileRequest;
import com.feadev.eaids.scoring.dto.response.AthleteScoreResponse;
import com.feadev.eaids.scoring.dto.response.WeightProfileResponse;

import java.util.List;
import java.util.UUID;

public interface ScoringService {

    WeightProfileResponse createWeightProfile(UUID campId, CreateWeightProfileRequest request);

    List<WeightProfileResponse> getWeightProfiles(UUID campId);

    List<AthleteScoreResponse> calculateScores(UUID campId);

    AthleteScoreResponse getAthleteScore(UUID enrollmentId);
}
