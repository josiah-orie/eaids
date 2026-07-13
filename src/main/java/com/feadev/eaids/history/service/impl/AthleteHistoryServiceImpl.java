package com.feadev.eaids.history.service.impl;

import com.feadev.eaids.development.model.DevelopmentPlan;
import com.feadev.eaids.development.repository.DevelopmentPlanRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.history.dto.response.AthleteHistoryResponse;
import com.feadev.eaids.history.dto.response.HistoryTimelineEntry;
import com.feadev.eaids.history.service.AthleteHistoryService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.selection.repository.SelectionDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AthleteHistoryServiceImpl implements AthleteHistoryService {

    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteScoreRepository scoreRepository;
    private final CoachEvaluationRepository evaluationRepository;
    private final SelectionDecisionRepository selectionRepository;
    private final DevelopmentPlanRepository developmentPlanRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    @Override
    public AthleteHistoryResponse getHistory(UUID athleteId, UUID campId, List<String> types,
                                              LocalDate fromDate, LocalDate toDate, int page, int size) {
        String athleteName = athleteProfileRepository.findByUser_Id(athleteId)
                .map(p -> p.getFullName())
                .orElseThrow(() -> new ResourceNotFoundException("Athlete not found: " + athleteId));

        // Get all enrollments for this athlete
        List<CampEnrollment> enrollments = enrollmentRepository.findByUserId(athleteId);

        if (campId != null) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getCamp().getId().equals(campId))
                    .collect(Collectors.toList());
        }

        List<HistoryTimelineEntry> allEntries = new ArrayList<>();

        for (CampEnrollment enrollment : enrollments) {
            UUID enrollmentId = enrollment.getId();
            UUID enrollmentCampId = enrollment.getCamp().getId();
            String campName = enrollment.getCamp().getName();

            // CAMP entries
            if (shouldInclude(types, "CAMP")) {
                allEntries.add(HistoryTimelineEntry.builder()
                        .type("CAMP")
                        .date(enrollment.getCreatedDate())
                        .summary("Enrolled in " + campName)
                        .data(Map.of(
                                "campId", enrollmentCampId,
                                "campName", campName,
                                "campType", enrollment.getCamp().getCampType().name(),
                                "startDate", enrollment.getCamp().getStartDate().toString(),
                                "endDate", enrollment.getCamp().getEndDate().toString(),
                                "enrollmentStatus", enrollment.getStatus().name()
                        ))
                        .build());
            }

            // SCORE entries
            if (shouldInclude(types, "SCORE")) {
                scoreRepository.findByEnrollmentId(enrollmentId).ifPresent(score -> {
                    allEntries.add(HistoryTimelineEntry.builder()
                            .type("SCORE")
                            .date(score.getCalculatedAt())
                            .summary("Score calculated: " + score.getTotalWeightedScore() + " in " + campName)
                            .data(Map.of(
                                    "campId", enrollmentCampId,
                                    "campName", campName,
                                    "totalScore", score.getTotalWeightedScore(),
                                    "physicalScore", nullSafe(score.getPhysicalScore()),
                                    "technicalScore", nullSafe(score.getTechnicalScore()),
                                    "tacticalScore", nullSafe(score.getTacticalScore()),
                                    "calculatedAt", score.getCalculatedAt().toString()
                            ))
                            .build());
                });
            }

            // EVALUATION entries
            if (shouldInclude(types, "EVALUATION")) {
                evaluationRepository.findByEnrollmentId(enrollmentId).forEach(eval -> {
                    allEntries.add(HistoryTimelineEntry.builder()
                            .type("EVALUATION")
                            .date(eval.getCreatedDate())
                            .summary("Coach evaluation in " + campName)
                            .data(Map.of(
                                    "campId", enrollmentCampId,
                                    "technicalScore", nullSafe(eval.getTechnicalScore()),
                                    "tacticalScore", nullSafe(eval.getTacticalScore()),
                                    "leadershipScore", nullSafe(eval.getLeadershipScore()),
                                    "characterScore", nullSafe(eval.getCharacterScore()),
                                    "date", eval.getCreatedDate().toString()
                            ))
                            .build());
                });
            }

            // SELECTION entries
            if (shouldInclude(types, "SELECTION")) {
                selectionRepository.findByEnrollmentId(enrollmentId).ifPresent(sel -> {
                    allEntries.add(HistoryTimelineEntry.builder()
                            .type("SELECTION")
                            .date(sel.getCreatedDate())
                            .summary("Selection: " + sel.getRecommendedTier().name() + " in " + campName)
                            .data(Map.of(
                                    "campId", enrollmentCampId,
                                    "recommendedTier", sel.getRecommendedTier().name(),
                                    "finalTier", sel.getFinalTier() != null ? sel.getFinalTier().name() : "",
                                    "approvalStatus", sel.getApprovalStatus().name()
                            ))
                            .build());
                });
            }
        }

        // DEVELOPMENT_PLAN entries
        if (shouldInclude(types, "DEVELOPMENT_PLAN")) {
            List<DevelopmentPlan> plans = developmentPlanRepository.findByAthleteUserId(athleteId);
            for (DevelopmentPlan plan : plans) {
                if (campId != null && !plan.getCampId().equals(campId)) continue;
                allEntries.add(HistoryTimelineEntry.builder()
                        .type("DEVELOPMENT_PLAN")
                        .date(plan.getCreatedDate())
                        .summary("Development plan: " + plan.getStatus().name())
                        .data(Map.of(
                                "planId", plan.getId(),
                                "campId", plan.getCampId(),
                                "status", plan.getStatus().name(),
                                "strengthsCount", plan.getStrengths().size(),
                                "weaknessesCount", plan.getWeaknesses().size(),
                                "goalsCount", plan.getGoals().size()
                        ))
                        .build());
            }
        }

        // Filter by date range
        if (fromDate != null) {
            LocalDateTime from = fromDate.atStartOfDay();
            allEntries.removeIf(e -> e.getDate() != null && e.getDate().isBefore(from));
        }
        if (toDate != null) {
            LocalDateTime to = toDate.plusDays(1).atStartOfDay();
            allEntries.removeIf(e -> e.getDate() != null && e.getDate().isAfter(to));
        }

        // Sort by date descending
        allEntries.sort((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });

        int totalEntries = allEntries.size();

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, allEntries.size());
        List<HistoryTimelineEntry> pagedEntries = start < allEntries.size()
                ? allEntries.subList(start, end) : List.of();

        return AthleteHistoryResponse.builder()
                .athleteId(athleteId)
                .athleteName(athleteName)
                .totalCamps(enrollments.size())
                .totalEntries(totalEntries)
                .page(page)
                .size(size)
                .timeline(pagedEntries)
                .build();
    }

    private boolean shouldInclude(List<String> types, String type) {
        return types == null || types.isEmpty() || types.contains(type);
    }

    private Object nullSafe(Object value) {
        return value != null ? value : "";
    }
}
