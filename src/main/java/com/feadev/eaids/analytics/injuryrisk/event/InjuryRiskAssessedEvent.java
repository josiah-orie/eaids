package com.feadev.eaids.analytics.injuryrisk.event;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InjuryRiskAssessedEvent extends BaseDomainEvent {

    private final UUID enrollmentId;
    private final UUID athleteUserId;
    private final RiskLevel riskLevel;
    private final int riskScore;

    public InjuryRiskAssessedEvent(UUID enrollmentId, UUID athleteUserId,
                                    RiskLevel riskLevel, int riskScore) {
        this.enrollmentId = enrollmentId;
        this.athleteUserId = athleteUserId;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
    }
}
