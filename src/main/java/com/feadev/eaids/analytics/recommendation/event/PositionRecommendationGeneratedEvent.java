package com.feadev.eaids.analytics.recommendation.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PositionRecommendationGeneratedEvent extends BaseDomainEvent {

    private final UUID enrollmentId;
    private final UUID athleteUserId;
    private final FootballPosition recommendedPosition;

    public PositionRecommendationGeneratedEvent(UUID enrollmentId, UUID athleteUserId,
                                                 FootballPosition recommendedPosition) {
        this.enrollmentId = enrollmentId;
        this.athleteUserId = athleteUserId;
        this.recommendedPosition = recommendedPosition;
    }
}
