package com.feadev.eaids.selection.dto.response;

import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import com.feadev.eaids.selection.model.enums.SelectionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionResponse {

    private UUID id;
    private UUID enrollmentId;
    private String athleteName;
    private UUID campId;
    private SelectionTier recommendedTier;
    private SelectionTier finalTier;
    private ApprovalStatus approvalStatus;
    private UUID reviewedBy;
    private String comments;
    private LocalDateTime approvalDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
