package com.feadev.eaids.discipline.service.impl;

import com.feadev.eaids.discipline.dto.request.RecordInfractionRequest;
import com.feadev.eaids.discipline.dto.response.DisciplineResponse;
import com.feadev.eaids.discipline.dto.response.DisciplineSummaryResponse;
import com.feadev.eaids.discipline.model.DisciplineRecord;
import com.feadev.eaids.discipline.repository.DisciplineRecordRepository;
import com.feadev.eaids.discipline.service.DisciplineService;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DisciplineServiceImpl implements DisciplineService {

    private final DisciplineRecordRepository disciplineRepository;
    private final CampEnrollmentRepository enrollmentRepository;

    @Override
    public DisciplineResponse recordInfraction(UUID campId, RecordInfractionRequest request, String recordedBy) {
        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to this camp");
        }

        DisciplineRecord record = DisciplineRecord.builder()
                .enrollment(enrollment)
                .campId(campId)
                .infractionType(request.getInfractionType())
                .penaltyPoints(request.getInfractionType().getDefaultPenaltyPoints())
                .description(request.getDescription())
                .infractionDate(request.getInfractionDate())
                .recordedBy(recordedBy)
                .build();

        DisciplineRecord saved = disciplineRepository.save(record);
        log.info("Infraction recorded for enrollment {} - type: {}, points: {}",
                request.getEnrollmentId(), request.getInfractionType(), saved.getPenaltyPoints());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplineResponse> getInfractionsByEnrollment(UUID enrollmentId) {
        return disciplineRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplineSummaryResponse getDisciplineSummary(UUID enrollmentId) {
        CampEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        List<DisciplineRecord> records = disciplineRepository.findByEnrollmentId(enrollmentId);
        int totalPenaltyPoints = disciplineRepository.sumPenaltyPointsByEnrollmentId(enrollmentId);

        // Score: starts at 100, subtract penalties, floor at 0
        BigDecimal score = BigDecimal.valueOf(Math.max(0, 100 - totalPenaltyPoints));

        return DisciplineSummaryResponse.builder()
                .enrollmentId(enrollmentId)
                .campId(enrollment.getCamp().getId())
                .totalInfractions(records.size())
                .totalPenaltyPoints(totalPenaltyPoints)
                .disciplineScore(score)
                .build();
    }

    private DisciplineResponse mapToResponse(DisciplineRecord record) {
        return DisciplineResponse.builder()
                .id(record.getId())
                .enrollmentId(record.getEnrollment().getId())
                .campId(record.getCampId())
                .infractionType(record.getInfractionType().name())
                .penaltyPoints(record.getPenaltyPoints())
                .description(record.getDescription())
                .infractionDate(record.getInfractionDate())
                .recordedBy(record.getRecordedBy())
                .createdDate(record.getCreatedDate())
                .build();
    }
}
