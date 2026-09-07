package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.CreateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.request.UpdateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.response.TrainingScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Admin operations for managing academy training schedules.
 */
public interface AcademyTrainingScheduleService {

    TrainingScheduleResponse create(CreateTrainingScheduleRequest request);

    TrainingScheduleResponse update(UUID scheduleId, UpdateTrainingScheduleRequest request);

    TrainingScheduleResponse get(UUID scheduleId);

    Page<TrainingScheduleResponse> list(String ageCategory, String targetType, String status,
                                        Pageable pageable);

    TrainingScheduleResponse publish(UUID scheduleId);

    TrainingScheduleResponse cancel(UUID scheduleId);
}
