package com.feadev.eaids.scouting.repository;

import com.feadev.eaids.scouting.model.ScoutAccessScope;
import com.feadev.eaids.scouting.model.enums.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScoutAccessScopeRepository extends JpaRepository<ScoutAccessScope, UUID> {

    List<ScoutAccessScope> findByScoutUserId(UUID scoutUserId);

    List<ScoutAccessScope> findByScoutUserIdAndScopeType(UUID scoutUserId, ScopeType scopeType);

    void deleteByScoutUserIdAndScopeTypeAndScopeValue(UUID scoutUserId, ScopeType scopeType, String scopeValue);
}
