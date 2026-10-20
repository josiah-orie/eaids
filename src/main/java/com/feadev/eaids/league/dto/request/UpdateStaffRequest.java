package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.LeagueStaffRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {
    private LeagueStaffRole staffRole;
    private String notes;
}
