package com.feadev.eaids.selection.dto.request;

import com.feadev.eaids.selection.model.enums.SelectionTier;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveSelectionRequest {

    @NotNull(message = "Final tier is required")
    private SelectionTier finalTier;

    private String comments;
}
