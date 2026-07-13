package com.feadev.eaids.matchstats.repository;

import com.feadev.eaids.matchstats.model.MatchStatDefinition;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchStatDefinitionRepository extends JpaRepository<MatchStatDefinition, UUID> {

    List<MatchStatDefinition> findByPositionGroupAndIsActiveTrue(PositionGroup positionGroup);

    List<MatchStatDefinition> findByIsActiveTrue();

    boolean existsByStatNameAndPositionGroup(String statName, PositionGroup positionGroup);
}
