package com.feadev.eaids.analytics.benchmarking.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.Getter;

import java.util.UUID;

@Getter
public class BenchmarkCapturedEvent extends BaseDomainEvent {

    private final UUID benchmarkProfileId;
    private final UUID enrollmentId;
    private final FootballPosition position;

    public BenchmarkCapturedEvent(UUID benchmarkProfileId, UUID enrollmentId, FootballPosition position) {
        this.benchmarkProfileId = benchmarkProfileId;
        this.enrollmentId = enrollmentId;
        this.position = position;
    }
}
