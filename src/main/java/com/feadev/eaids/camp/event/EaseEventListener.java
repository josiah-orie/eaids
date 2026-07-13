package com.feadev.eaids.camp.event;

import com.feadev.eaids.enrollment.event.AthleteEnrolledEvent;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.scoring.event.ScoresCalculatedEvent;
import com.feadev.eaids.ranking.event.RankingsGeneratedEvent;
import com.feadev.eaids.selection.event.SelectionApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EaseEventListener {

    private final AuditService auditService;

    @EventListener
    public void handleCampStatusChanged(CampStatusChangedEvent event) {
        log.info("Camp {} status changed: {} -> {}", event.getCampId(), event.getPreviousStatus(), event.getNewStatus());
        auditService.log(
                AuditAction.CAMP_STATUS_CHANGED,
                null,
                "system",
                null,
                null
        );
    }

    @EventListener
    public void handleAthleteEnrolled(AthleteEnrolledEvent event) {
        log.info("Athlete {} enrolled in camp {} with status {}",
                event.getAthleteUserId(), event.getCampId(), event.getStatus());
        auditService.log(
                AuditAction.ATHLETE_ENROLLED,
                event.getAthleteUserId(),
                event.getAthleteUserId().toString(),
                null,
                null
        );
    }

    @EventListener
    public void handleScoresCalculated(ScoresCalculatedEvent event) {
        log.info("Scores calculated for camp {}", event.getCampId());
        auditService.log(
                AuditAction.SCORES_CALCULATED,
                null,
                "system",
                null,
                null
        );
    }

    @EventListener
    public void handleRankingsGenerated(RankingsGeneratedEvent event) {
        log.info("Rankings generated for camp {}", event.getCampId());
        auditService.log(
                AuditAction.RANKINGS_GENERATED,
                null,
                "system",
                null,
                null
        );
    }

    @EventListener
    public void handleSelectionApproved(SelectionApprovedEvent event) {
        log.info("Selection {} approved for camp {}", event.getSelectionId(), event.getCampId());
        auditService.log(
                AuditAction.SELECTION_APPROVED,
                null,
                "admin",
                null,
                null
        );
    }
}
