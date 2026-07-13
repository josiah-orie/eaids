package com.feadev.eaids.matchstats.util;

import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import com.feadev.eaids.technical.model.enums.FootballPosition;

import java.util.Map;

public final class PositionGroupMapper {

    private static final Map<FootballPosition, PositionGroup> MAPPING = Map.ofEntries(
            Map.entry(FootballPosition.QUARTERBACK, PositionGroup.QUARTERBACK),
            Map.entry(FootballPosition.WIDE_RECEIVER, PositionGroup.RECEIVER),
            Map.entry(FootballPosition.TIGHT_END, PositionGroup.RECEIVER),
            Map.entry(FootballPosition.RUNNING_BACK, PositionGroup.RECEIVER),
            Map.entry(FootballPosition.DEFENSIVE_LINE, PositionGroup.DEFENSE),
            Map.entry(FootballPosition.LINEBACKER, PositionGroup.DEFENSE),
            Map.entry(FootballPosition.CORNERBACK, PositionGroup.DEFENSE),
            Map.entry(FootballPosition.SAFETY, PositionGroup.DEFENSE),
            Map.entry(FootballPosition.OFFENSIVE_LINE, PositionGroup.DEFENSE),
            Map.entry(FootballPosition.KICKER, PositionGroup.SPECIAL_TEAMS),
            Map.entry(FootballPosition.PUNTER, PositionGroup.SPECIAL_TEAMS),
            Map.entry(FootballPosition.RETURNER, PositionGroup.SPECIAL_TEAMS),
            Map.entry(FootballPosition.ATHLETE, PositionGroup.DEFENSE)
    );

    private PositionGroupMapper() {
    }

    public static PositionGroup fromFootballPosition(FootballPosition position) {
        return MAPPING.getOrDefault(position, PositionGroup.DEFENSE);
    }
}
