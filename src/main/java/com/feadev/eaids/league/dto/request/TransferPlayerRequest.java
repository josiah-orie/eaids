package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.TransferType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferPlayerRequest {

    @NotNull(message = "Target team ID is required")
    private UUID toTeamId;

    @NotNull(message = "Transfer type is required")
    private TransferType transferType;

    private String notes;
}
