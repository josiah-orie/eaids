package com.feadev.eaids.matchstats.config;

import com.feadev.eaids.matchstats.model.MatchStatDefinition;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import com.feadev.eaids.matchstats.repository.MatchStatDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class MatchStatDefinitionSeeder implements CommandLineRunner {

    private final MatchStatDefinitionRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        List<MatchStatDefinition> definitions = List.of(
                // Quarterback stats
                buildDef(PositionGroup.QUARTERBACK, "Attempts", "count", 1),
                buildDef(PositionGroup.QUARTERBACK, "Completions", "count", 2),
                buildDef(PositionGroup.QUARTERBACK, "Passing Yards", "yards", 3),
                buildDef(PositionGroup.QUARTERBACK, "Touchdown Passes", "count", 4),
                buildDef(PositionGroup.QUARTERBACK, "Interceptions", "count", 5),
                // Receiver stats
                buildDef(PositionGroup.RECEIVER, "Targets", "count", 1),
                buildDef(PositionGroup.RECEIVER, "Receptions", "count", 2),
                buildDef(PositionGroup.RECEIVER, "Receiving Yards", "yards", 3),
                buildDef(PositionGroup.RECEIVER, "Receiving Touchdowns", "count", 4),
                // Defense stats
                buildDef(PositionGroup.DEFENSE, "Tackles", "count", 1),
                buildDef(PositionGroup.DEFENSE, "Sacks", "count", 2),
                buildDef(PositionGroup.DEFENSE, "Interceptions", "count", 3),
                buildDef(PositionGroup.DEFENSE, "Forced Fumbles", "count", 4)
        );

        repository.saveAll(definitions);
        log.info("Seeded {} match stat definitions", definitions.size());
    }

    private MatchStatDefinition buildDef(PositionGroup group, String name, String unit, int order) {
        return MatchStatDefinition.builder()
                .positionGroup(group)
                .statName(name)
                .unit(unit)
                .displayOrder(order)
                .isActive(true)
                .build();
    }
}
