package com.feadev.eaids.dashboard.service.impl;

import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.dashboard.dto.response.AdminDashboardResponse;
import com.feadev.eaids.dashboard.service.AdminDashboardService;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import com.feadev.eaids.development.repository.DevelopmentPlanRepository;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.repository.SelectionDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final CoachEvaluationRepository evaluationRepository;
    private final SelectionDecisionRepository selectionRepository;
    private final AthleteScoreRepository scoreRepository;
    private final DevelopmentPlanRepository planRepository;

    @Override
    public AdminDashboardResponse getAdminDashboard(int periodDays) {
        // User stats
        long totalAthletes = countUsersByRole(UserRole.ROLE_ATHLETE);
        long totalCoaches = countUsersByRole(UserRole.ROLE_COACH);
        long totalAdmins = countUsersByRole(UserRole.ROLE_ADMIN);
        long activeAthletes = countUsersByRoleAndStatus(UserRole.ROLE_ATHLETE, UserStatus.ACTIVE);
        long pendingReview = countUsersByStatus(UserStatus.UNDER_REVIEW);
        long suspendedAccounts = countUsersByStatus(UserStatus.SUSPENDED);

        AdminDashboardResponse.UserStats userStats = AdminDashboardResponse.UserStats.builder()
                .totalAthletes(totalAthletes)
                .totalCoaches(totalCoaches)
                .totalAdmins(totalAdmins)
                .activeAthletes(activeAthletes)
                .pendingReview(pendingReview)
                .suspendedAccounts(suspendedAccounts)
                .build();

        // Camp stats
        long totalCamps = campRepository.count();
        long activeCamps = campRepository.findByStatus(CampStatus.IN_PROGRESS).size();
        long completedCamps = campRepository.findByStatus(CampStatus.COMPLETED).size();
        long totalEnrollments = enrollmentRepository.count();
        double avgEnrollment = totalCamps > 0 ? (double) totalEnrollments / totalCamps : 0;

        AdminDashboardResponse.CampStats campStats = AdminDashboardResponse.CampStats.builder()
                .totalCamps(totalCamps)
                .activeCamps(activeCamps)
                .completedCamps(completedCamps)
                .totalEnrollments(totalEnrollments)
                .averageEnrollmentPerCamp(Math.round(avgEnrollment * 100.0) / 100.0)
                .build();

        // Evaluation stats
        long totalEvaluations = evaluationRepository.count();
        AdminDashboardResponse.EvaluationStats evaluationStats = AdminDashboardResponse.EvaluationStats.builder()
                .totalEvaluations(totalEvaluations)
                .evaluationsThisPeriod(totalEvaluations) // TODO: filter by period
                .build();

        // Selection tier distribution
        List<SelectionDecision> allSelections = selectionRepository.findAll();
        List<AdminDashboardResponse.TierDistributionItem> tierDistribution = new ArrayList<>();
        if (!allSelections.isEmpty()) {
            Map<String, Long> tierCounts = allSelections.stream()
                    .filter(s -> s.getRecommendedTier() != null)
                    .collect(Collectors.groupingBy(s -> s.getRecommendedTier().name(), Collectors.counting()));
            long totalSelections = allSelections.size();
            tierCounts.forEach((tier, count) -> {
                tierDistribution.add(AdminDashboardResponse.TierDistributionItem.builder()
                        .tier(tier)
                        .count(count)
                        .percentage(Math.round(count * 10000.0 / totalSelections) / 100.0)
                        .build());
            });
        }

        // Scoring overview
        List<AthleteScore> allScores = scoreRepository.findAll();
        BigDecimal avgScore = BigDecimal.ZERO;
        BigDecimal highestScore = BigDecimal.ZERO;
        BigDecimal lowestScore = BigDecimal.ZERO;
        if (!allScores.isEmpty()) {
            List<BigDecimal> totals = allScores.stream()
                    .map(AthleteScore::getTotalWeightedScore)
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
            if (!totals.isEmpty()) {
                highestScore = totals.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                lowestScore = totals.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                avgScore = totals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(totals.size()), 2, RoundingMode.HALF_UP);
            }
        }

        AdminDashboardResponse.ScoringOverview scoringOverview = AdminDashboardResponse.ScoringOverview.builder()
                .averageOverallScore(avgScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .build();

        // Development stats
        long totalPlans = planRepository.count();
        long activePlansCount = planRepository.countByStatus(DevelopmentPlanStatus.ACTIVE);
        long completedPlansCount = planRepository.countByStatus(DevelopmentPlanStatus.COMPLETED);

        AdminDashboardResponse.DevelopmentStats developmentStats = AdminDashboardResponse.DevelopmentStats.builder()
                .totalPlans(totalPlans)
                .activePlans(activePlansCount)
                .completedPlans(completedPlansCount)
                .build();

        return AdminDashboardResponse.builder()
                .userStats(userStats)
                .campStats(campStats)
                .evaluationStats(evaluationStats)
                .selectionTierDistribution(tierDistribution)
                .scoringOverview(scoringOverview)
                .developmentStats(developmentStats)
                .build();
    }

    private long countUsersByRole(UserRole role) {
        Specification<com.feadev.eaids.registrations.model.User> spec = (root, query, cb) ->
                cb.equal(root.get("role"), role);
        return userRepository.count(spec);
    }

    private long countUsersByRoleAndStatus(UserRole role, UserStatus status) {
        Specification<com.feadev.eaids.registrations.model.User> spec = (root, query, cb) ->
                cb.and(cb.equal(root.get("role"), role), cb.equal(root.get("status"), status));
        return userRepository.count(spec);
    }

    private long countUsersByStatus(UserStatus status) {
        Specification<com.feadev.eaids.registrations.model.User> spec = (root, query, cb) ->
                cb.equal(root.get("status"), status);
        return userRepository.count(spec);
    }
}
