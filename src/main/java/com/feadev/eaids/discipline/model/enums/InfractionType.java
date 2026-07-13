package com.feadev.eaids.discipline.model.enums;

public enum InfractionType {
    WARNING(1),
    YELLOW_CARD(3),
    RED_CARD(5),
    SUSPENSION(10);

    private final int defaultPenaltyPoints;

    InfractionType(int defaultPenaltyPoints) {
        this.defaultPenaltyPoints = defaultPenaltyPoints;
    }

    public int getDefaultPenaltyPoints() {
        return defaultPenaltyPoints;
    }
}
