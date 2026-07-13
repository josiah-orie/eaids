package com.feadev.eaids.analytics.prediction.event;

import com.feadev.eaids.analytics.prediction.model.enums.PredictionTier;
import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PotentialPredictionGeneratedEvent extends BaseDomainEvent {

    private final UUID athleteUserId;
    private final PredictionTier predictionTier;

    public PotentialPredictionGeneratedEvent(UUID athleteUserId, PredictionTier predictionTier) {
        this.athleteUserId = athleteUserId;
        this.predictionTier = predictionTier;
    }
}
