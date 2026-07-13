package com.feadev.eaids.discipline.dto.request;

import com.feadev.eaids.discipline.model.enums.InfractionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordInfractionRequest {

    @NotNull(message = "Enrollment ID is required")
    private UUID enrollmentId;

    @NotNull(message = "Infraction type is required")
    private InfractionType infractionType;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Infraction date is required")
    private LocalDate infractionDate;
}
