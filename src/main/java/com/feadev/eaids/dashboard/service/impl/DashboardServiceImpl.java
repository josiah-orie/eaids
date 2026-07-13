package com.feadev.eaids.dashboard.service.impl;

import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.dashboard.dto.response.AthleteDashboardResponse;
import com.feadev.eaids.dashboard.dto.response.CoachDashboardResponse;
import com.feadev.eaids.dashboard.service.DashboardService;
import com.feadev.eaids.development.model.DevelopmentPlan;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import com.feadev.eaids.development.model.enums.GoalStatus;
import com.feadev.eaids.development.repository.DevelopmentGoalRepository;
import com.feadev.eaids.development.repository.DevelopmentPlanRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.model.CampEvaluator;
import com.feadev.eaids.evaluation.model.CoachEvaluation;
import com.feadev.eaids.evaluation.repository.CampEvaluatorRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.repository.AthleteRankingRepository;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
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
public class DashboardServiceImpl implements DashboardService {

    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteScoreRepository scoreRepository;
    private final AthleteRankingRepository rankingRepository;
    private final CoachEvaluationRepository evaluationRepository;
    private final CampEvaluatorRepository evaluatorRepository;
    private final DevelopmentPlanRepository planRepository;
    private final DevelopmentGoalRepository goalRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    @Override
    public AthleteDashboardResponse getAthleteDashboard(UUID athleteUserId) {
        List<CampEnrollment> enrollments = enrollmentRepository.findByUserId(athleteUserId);

        // Performance trend
        List<AthleteDashboardResponse.PerformanceTrendItem> trend = new ArrayList<>();
        for (CampEnrollment enrollment : enrollments) {
            scoreRepository.findByEnrollmentId(enrollment.getId()).ifPresent(score -> {
                trend.add(AthleteDashboardResponse.PerformanceTrendItem.builder()
                        .campName(enrollment.getCamp().getName())
                        .date(enrollment.getCamp().getStartDate())
                        .totalScore(score.getTotalWeightedScore())
                        .build());
            });
        }
        trend.sort(Comparator.comparing(AthleteDashboardResponse.PerformanceTrendItem::getDate));

        // Latest rankings (from most recent camp with scores)
        List<AthleteDashboardResponse.RankingItem> rankings = new ArrayList<>();
        if (!enrollments.isEmpty()) {
            // Find most recent enrollment with rankings
            for (int i = enrollments.size() - 1; i >= 0; i--) {
                List<AthleteRanking> campRankings = rankingRepository.findByEnrollmentId(enrollments.get(i).getId());
                if (!campRankings.isEmpty()) {
                    rankings = campRankings.stream()
                            .map(r -> AthleteDashboardResponse.RankingItem.builder()
                                    .rankingType(r.getRankingType().name())
                                    .rankValue(r.getRankValue())
                                    .totalInCategory(r.getTotalInCategory())
                                    .percentile(r.getPercentile())
                                    .build())
                            .collect(Collectors.toList());
                    break;
                }
            }
        }

        // Development plan summary
        List<DevelopmentPlan> activePlans = planRepository.findByAthleteUserIdAndStatus(athleteUserId, DevelopmentPlanStatus.ACTIVE);
        long goalsTotal = 0;
        long goalsAchieved = 0;
        for (DevelopmentPlan plan : activePlans) {
            goalsTotal += plan.getGoals().size();
            goalsAchieved += plan.getGoals().stream().filter(g -> g.getStatus() == GoalStatus.ACHIEVED).count();
        }
        double progressPct = goalsTotal > 0 ? (goalsAchieved * 100.0 / goalsTotal) : 0;

        AthleteDashboardResponse.DevelopmentPlanSummary devSummary = AthleteDashboardResponse.DevelopmentPlanSummary.builder()
                .activePlans(activePlans.size())
                .goalsTotal(goalsTotal)
                .goalsAchieved(goalsAchieved)
                .progressPercentage(progressPct)
                .build();

        // Upcoming camps
        List<AthleteDashboardResponse.UpcomingCampItem> upcomingCamps = enrollments.stream()
                .filter(e -> e.getCamp().getStatus() != CampStatus.COMPLETED && e.getCamp().getStatus() != CampStatus.ARCHIVED)
                .map(e -> AthleteDashboardResponse.UpcomingCampItem.builder()
                        .campId(e.getCamp().getId())
                        .name(e.getCamp().getName())
                        .startDate(e.getCamp().getStartDate())
                        .enrollmentStatus(e.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        // Recent evaluations (last 5)
        List<AthleteDashboardResponse.RecentEvaluationItem> recentEvals = new ArrayList<>();
        for (CampEnrollment enrollment : enrollments) {
            List<CoachEvaluation> evals = evaluationRepository.findByEnrollmentId(enrollment.getId());
            for (CoachEvaluation eval : evals) {
                recentEvals.add(AthleteDashboardResponse.RecentEvaluationItem.builder()
                        .evaluatorName("Coach")
                        .technicalScore(eval.getTechnicalScore())
                        .tacticalScore(eval.getTacticalScore())
                        .leadershipScore(eval.getLeadershipScore())
                        .characterScore(eval.getCharacterScore())
                        .date(eval.getCreatedDate())
                        .build());
            }
        }
        recentEvals.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        if (recentEvals.size() > 5) recentEvals = recentEvals.subList(0, 5);

        return AthleteDashboardResponse.builder()
                .performanceTrend(trend)
                .latestRankings(rankings)
                .developmentPlanSummary(devSummary)
                .upcomingCamps(upcomingCamps)
                .recentEvaluations(recentEvals)
                .build();
    }

    @Override
    public CoachDashboardResponse getCoachDashboard(UUID coachUserId) {
        // Assigned athletes from development plans
        List<DevelopmentPlan> coachPlans = planRepository.findByCoachUserId(coachUserId);
        long assignedAthletes = coachPlans.stream()
                .map(DevelopmentPlan::getAthleteUserId)
                .distinct()
                .count();

        // Evaluation activity
        List<CampEvaluator> evaluatorEntries = evaluatorRepository.findByUserId(coachUserId);
        long totalEvals = 0;
        long thisMonth = 0;
        long lastMonth = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        List<CoachDashboardResponse.RecentEvaluationItem> recentEvals = new ArrayList<>();

        for (CampEvaluator evaluator : evaluatorEntries) {
            List<CoachEvaluation> evals = evaluationRepository.findByEvaluatorId(evaluator.getId());
            totalEvals += evals.size();
            for (CoachEvaluation eval : evals) {
                if (eval.getCreatedDate().isAfter(startOfMonth)) thisMonth++;
                else if (eval.getCreatedDate().isAfter(startOfLastMonth)) lastMonth++;

                String athleteName = athleteProfileRepository.findByUser_Id(eval.getEnrollment().getUser().getId())
                        .map(p -> p.getFullName()).orElse("Unknown");
                recentEvals.add(CoachDashboardResponse.RecentEvaluationItem.builder()
                        .athleteName(athleteName)
                        .technicalScore(eval.getTechnicalScore())
                        .tacticalScore(eval.getTacticalScore())
                        .date(eval.getCreatedDate())
                        .build());
            }
        }

        recentEvals.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        if (recentEvals.size() > 10) recentEvals = recentEvals.subList(0, 10);

        CoachDashboardResponse.EvaluationActivity evalActivity = CoachDashboardResponse.EvaluationActivity.builder()
                .totalEvaluationsSubmitted(totalEvals)
                .thisMonth(thisMonth)
                .lastMonth(lastMonth)
                .build();

        // Development plans summary
        long activePlansCount = coachPlans.stream().filter(p -> p.getStatus() == DevelopmentPlanStatus.ACTIVE).count();
        long completedPlansCount = coachPlans.stream().filter(p -> p.getStatus() == DevelopmentPlanStatus.COMPLETED).count();

        CoachDashboardResponse.DevelopmentPlansSummary plansSummary = CoachDashboardResponse.DevelopmentPlansSummary.builder()
                .activePlans(activePlansCount)
                .completedPlans(completedPlansCount)
                .build();

        // Camp activity
        List<CoachDashboardResponse.CampActivityItem> campActivity = evaluatorEntries.stream()
                .map(e -> CoachDashboardResponse.CampActivityItem.builder()
                        .campId(e.getCamp().getId())
                        .campName(e.getCamp().getName())
                        .isLead(e.isLead())
                        .startDate(e.getCamp().getStartDate())
                        .endDate(e.getCamp().getEndDate())
                        .build())
                .collect(Collectors.toList());

        return CoachDashboardResponse.builder()
                .assignedAthletesCount(assignedAthletes)
                .evaluationActivity(evalActivity)
                .developmentPlansSummary(plansSummary)
                .campActivity(campActivity)
                .recentEvaluations(recentEvals)
                .build();
    }
}
