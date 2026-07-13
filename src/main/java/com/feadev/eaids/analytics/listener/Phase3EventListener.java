package com.feadev.eaids.analytics.listener;

import com.feadev.eaids.analytics.benchmarking.event.BenchmarkCapturedEvent;
import com.feadev.eaids.analytics.benchmarking.service.BenchmarkingService;
import com.feadev.eaids.analytics.injuryrisk.event.InjuryRiskAssessedEvent;
import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.analytics.prediction.event.PotentialPredictionGeneratedEvent;
import com.feadev.eaids.analytics.recommendation.event.PositionRecommendationGeneratedEvent;
import com.feadev.eaids.nationalranking.event.NationalRankingsGeneratedEvent;
import com.feadev.eaids.scouting.event.InterviewRequestApprovedEvent;
import com.feadev.eaids.scouting.event.InterviewRequestRejectedEvent;
import com.feadev.eaids.scouting.event.InterviewRequestSubmittedEvent;
import com.feadev.eaids.selection.event.SelectionApprovedEvent;
import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.model.enums.SelectionTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Central event listener for Phase 3 domain events.
 * Handles:
 * - Auto-capture benchmarks from TIER_1_ELITE selections
 * - Email notifications for interview approvals/rejections
 * - Email alerts for high/critical injury risk
 * - Admin notifications for national ranking generation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Phase3EventListener {

    private final BenchmarkingService benchmarkingService;
    private final JavaMailSender mailSender;

    /**
     * When a selection is approved, check if it's TIER_1_ELITE and auto-capture benchmark.
     */
    @EventListener
    @Async
    public void handleSelectionApproved(SelectionApprovedEvent event) {
        log.info("Handling SelectionApprovedEvent: selectionId={}, enrollmentId={}",
                event.getSelectionId(), event.getEnrollmentId());

        // Auto-capture benchmark from elite selection
        // The BenchmarkingService will check the tier internally by loading the decision
        try {
            benchmarkingService.autoCaptureFromEliteSelection(event.getEnrollmentId());
        } catch (Exception e) {
            log.error("Failed to auto-capture benchmark for enrollment {}: {}",
                    event.getEnrollmentId(), e.getMessage());
        }
    }

    /**
     * When injury risk is assessed as HIGH or CRITICAL, send email alert.
     */
    @EventListener
    @Async
    public void handleInjuryRiskAssessed(InjuryRiskAssessedEvent event) {
        if (event.getRiskLevel() == RiskLevel.HIGH || event.getRiskLevel() == RiskLevel.CRITICAL) {
            log.warn("HIGH/CRITICAL injury risk detected: enrollmentId={}, riskScore={}, level={}",
                    event.getEnrollmentId(), event.getRiskScore(), event.getRiskLevel());

            // In production, lookup the assigned coach's email and send notification
            // For now, log the alert
            log.info("ALERT: Coach notification needed for injury risk - enrollment {}",
                    event.getEnrollmentId());
        }
    }

    /**
     * When national rankings are generated, log and notify admins.
     */
    @EventListener
    @Async
    public void handleNationalRankingsGenerated(NationalRankingsGeneratedEvent event) {
        log.info("National rankings generated: configId={}, totalRankings={}",
                event.getConfigId(), event.getTotalRankingsGenerated());

        // In production, send email summary to all admins
        log.info("NOTIFICATION: Admin email needed - {} national rankings generated",
                event.getTotalRankingsGenerated());
    }

    /**
     * When an interview request is approved, notify the athlete.
     */
    @EventListener
    @Async
    public void handleInterviewRequestApproved(InterviewRequestApprovedEvent event) {
        log.info("Interview request approved: id={}, athlete={}",
                event.getInterviewRequestId(), event.getAthleteUserId());

        // In production, lookup athlete email and send notification
        log.info("NOTIFICATION: Athlete {} should be notified of interview approval",
                event.getAthleteUserId());
    }

    /**
     * When an interview request is rejected, notify the scout.
     */
    @EventListener
    @Async
    public void handleInterviewRequestRejected(InterviewRequestRejectedEvent event) {
        log.info("Interview request rejected: id={}, scout={}",
                event.getInterviewRequestId(), event.getScoutUserId());

        // In production, lookup scout email and send notification
        log.info("NOTIFICATION: Scout {} should be notified of interview rejection",
                event.getScoutUserId());
    }

    /**
     * Log position recommendation events for audit trail.
     */
    @EventListener
    @Async
    public void handlePositionRecommendation(PositionRecommendationGeneratedEvent event) {
        log.info("Position recommendation generated: enrollmentId={}, position={}",
                event.getEnrollmentId(), event.getRecommendedPosition());
    }

    /**
     * Log potential prediction events.
     */
    @EventListener
    @Async
    public void handlePotentialPrediction(PotentialPredictionGeneratedEvent event) {
        log.info("Potential prediction generated: athleteUserId={}, tier={}",
                event.getAthleteUserId(), event.getPredictionTier());
    }

    /**
     * Log benchmark capture events.
     */
    @EventListener
    @Async
    public void handleBenchmarkCaptured(BenchmarkCapturedEvent event) {
        log.info("Benchmark auto-captured: benchmarkId={}, enrollmentId={}, position={}",
                event.getBenchmarkProfileId(), event.getEnrollmentId(), event.getPosition());
    }

    /**
     * Log interview request submission.
     */
    @EventListener
    @Async
    public void handleInterviewRequestSubmitted(InterviewRequestSubmittedEvent event) {
        log.info("Interview request submitted: id={}, scout={}, athlete={}",
                event.getInterviewRequestId(), event.getScoutUserId(), event.getAthleteUserId());
    }
}
