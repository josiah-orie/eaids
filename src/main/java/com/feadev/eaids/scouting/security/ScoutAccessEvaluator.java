package com.feadev.eaids.scouting.security;

import com.feadev.eaids.scouting.model.ScoutAccessScope;
import com.feadev.eaids.scouting.model.enums.ScopeType;
import com.feadev.eaids.scouting.repository.ScoutAccessScopeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates whether a scout has access to view a given athlete's data
 * based on their assigned access scopes (country, state, camp, age group, position).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScoutAccessEvaluator {

    private final ScoutAccessScopeRepository scopeRepository;

    /**
     * Check if a scout has access to an athlete based on athlete attributes.
     */
    public boolean hasAccess(UUID scoutUserId, String athleteCountry, String athleteState,
                              String athleteAgeCategory, String athletePosition) {
        List<ScoutAccessScope> scopes = scopeRepository.findByScoutUserId(scoutUserId);

        if (scopes.isEmpty()) {
            log.debug("Scout {} has no access scopes", scoutUserId);
            return false;
        }

        // Filter out expired scopes
        LocalDateTime now = LocalDateTime.now();
        List<ScoutAccessScope> activeScopes = scopes.stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(now))
                .toList();

        if (activeScopes.isEmpty()) {
            return false;
        }

        // Check each scope type - athlete must match ALL scope types that the scout has
        // (intersection logic: if scout has COUNTRY=Nigeria, athlete must be from Nigeria)
        boolean countryMatch = checkScopeMatch(activeScopes, ScopeType.COUNTRY, athleteCountry);
        boolean stateMatch = checkScopeMatch(activeScopes, ScopeType.STATE, athleteState);
        boolean ageMatch = checkScopeMatch(activeScopes, ScopeType.AGE_GROUP, athleteAgeCategory);
        boolean positionMatch = checkScopeMatch(activeScopes, ScopeType.POSITION, athletePosition);

        return countryMatch && stateMatch && ageMatch && positionMatch;
    }

    /**
     * For a given scope type, check if the scout has any restrictions.
     * If the scout has no scopes of that type, it means no restriction (allow all).
     * If the scout has scopes of that type, the value must match one of them.
     */
    private boolean checkScopeMatch(List<ScoutAccessScope> scopes, ScopeType type, String value) {
        List<ScoutAccessScope> scopesOfType = scopes.stream()
                .filter(s -> s.getScopeType() == type)
                .toList();

        // No scopes of this type = no restriction
        if (scopesOfType.isEmpty()) {
            return true;
        }

        // Value is null/unknown = cannot verify, deny
        if (value == null || value.isBlank()) {
            return false;
        }

        // Must match at least one scope value (case-insensitive)
        return scopesOfType.stream()
                .anyMatch(s -> s.getScopeValue().equalsIgnoreCase(value));
    }
}
