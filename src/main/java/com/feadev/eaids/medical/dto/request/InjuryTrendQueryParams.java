package com.feadev.eaids.medical.dto.request;

import com.feadev.eaids.medical.model.enums.BodyRegion;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Query parameters for the injury trend dashboard endpoint.
 * All fields are optional; defaults are applied in the service layer.
 */
@Data
public class InjuryTrendQueryParams {

    /** Filter by team (by team_id from the league module). */
    private UUID teamId;

    /** Filter by league (resolves all team players in that league). */
    private UUID leagueId;

    /** Filter to a specific body region. */
    private BodyRegion bodyRegion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
