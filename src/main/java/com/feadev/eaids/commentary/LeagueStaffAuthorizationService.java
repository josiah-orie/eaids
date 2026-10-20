package com.feadev.eaids.commentary;

import com.feadev.eaids.league.model.enums.LeagueStaffRole;
import com.feadev.eaids.league.repository.LeagueStaffRepository;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SpEL-accessible authorization bean used in {@code @PreAuthorize} expressions.
 *
 * <pre>
 * @PreAuthorize("hasRole('ADMIN') or @leagueStaffAuthz.isCoordinatorForFixture(#fixtureId, authentication)")
 * </pre>
 */
@Component("leagueStaffAuthz")
@RequiredArgsConstructor
@Slf4j
public class LeagueStaffAuthorizationService {

    private final MatchFixtureRepository fixtureRepository;
    private final LeagueStaffRepository leagueStaffRepository;

    /**
     * Returns {@code true} if the authenticated user is a LEAGUE_COORDINATOR
     * for the league that owns the given fixture.
     */
    public boolean isCoordinatorForFixture(UUID fixtureId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        try {
            UUID leagueId = fixtureRepository.findById(fixtureId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fixture not found: " + fixtureId))
                    .getLeague().getId();
            return isCoordinatorForLeague(leagueId, authentication);
        } catch (ResourceNotFoundException e) {
            log.warn("isCoordinatorForFixture: fixture {} not found", fixtureId);
            return false;
        }
    }

    /**
     * Returns {@code true} if the authenticated user is a LEAGUE_COORDINATOR for the given league.
     */
    public boolean isCoordinatorForLeague(UUID leagueId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        UUID userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }
        return leagueStaffRepository.existsByLeagueIdAndUserIdAndStaffRole(
                leagueId, userId, LeagueStaffRole.LEAGUE_COORDINATOR);
    }

    private UUID extractUserId(Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof CustomUserDetails details) {
                return details.getId();
            }
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            log.warn("Could not extract user ID from authentication: {}", e.getMessage());
            return null;
        }
    }
}
