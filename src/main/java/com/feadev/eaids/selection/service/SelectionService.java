package com.feadev.eaids.selection.service;

import com.feadev.eaids.selection.dto.request.ApproveSelectionRequest;
import com.feadev.eaids.selection.dto.response.SelectionResponse;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;

import java.util.List;
import java.util.UUID;

public interface SelectionService {

    List<SelectionResponse> generateSelections(UUID campId);

    List<SelectionResponse> getSelections(UUID campId, ApprovalStatus status);

    SelectionResponse approveSelection(UUID selectionId, ApproveSelectionRequest request, UUID reviewerId);

    SelectionResponse rejectSelection(UUID selectionId, String comments, UUID reviewerId);
}
