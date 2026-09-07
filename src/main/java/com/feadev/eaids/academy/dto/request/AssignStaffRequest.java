package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.StaffType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Admin payload to assign a coach or scout to a training schedule.
 * {@code profileId} references a CoachProfile id (COACH) or ScoutProfile id (SCOUT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffRequest {

    @NotNull(message = "Staff type is required")
    private StaffType staffType;

    @NotNull(message = "Profile id is required")
    private UUID profileId;
}
