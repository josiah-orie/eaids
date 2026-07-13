package com.feadev.eaids.trainingload.service;

import com.feadev.eaids.trainingload.dto.request.CreateTrainingLoadRequest;
import com.feadev.eaids.trainingload.dto.response.TrainingLoadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TrainingLoadService {

    TrainingLoadResponse recordTrainingLoad(UUID campId, UUID enrollmentId, CreateTrainingLoadRequest request);

    Page<TrainingLoadResponse> getTrainingLoadHistory(UUID enrollmentId, Pageable pageable);

    TrainingLoadResponse getTrainingLoadById(UUID id);

    void deleteTrainingLoad(UUID id);
}
