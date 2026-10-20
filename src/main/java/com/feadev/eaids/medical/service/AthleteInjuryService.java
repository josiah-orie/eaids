package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.request.ReportInjuryRequest;
import com.feadev.eaids.medical.dto.request.UpdateInjuryRequest;
import com.feadev.eaids.medical.model.enums.InjuryStatus;
import com.feadev.eaids.registrations.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AthleteInjuryService {

    Object reportInjury(UUID athleteUserId, ReportInjuryRequest request,
                        UUID reportedBy, UserRole actorRole);

    Object updateInjury(UUID injuryId, UpdateInjuryRequest request,
                        UUID actorId, UserRole actorRole);

    Object transitionStatus(UUID injuryId, InjuryStatus newStatus,
                             UUID actorId, UserRole actorRole);

    Object getInjury(UUID injuryId, UUID actorId, UserRole actorRole);

    Page<?> getInjuriesForAthlete(UUID athleteUserId, UUID actorId,
                                   UserRole actorRole, Pageable pageable);
}
