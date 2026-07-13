package com.feadev.eaids.camp.service;

import com.feadev.eaids.camp.dto.request.CreateCampRequest;
import com.feadev.eaids.camp.dto.request.UpdateCampRequest;
import com.feadev.eaids.camp.dto.response.CampResponse;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;

import java.util.List;
import java.util.UUID;

public interface CampService {

    CampResponse createCamp(CreateCampRequest request);

    CampResponse getCamp(UUID campId);

    List<CampResponse> getAllCamps();

    List<CampResponse> getCampsByStatus(CampStatus status);

    List<CampResponse> getCampsByType(CampType type);

    CampResponse updateCamp(UUID campId, UpdateCampRequest request);

    void deleteCamp(UUID campId);

    CampResponse transitionStatus(UUID campId, CampStatus newStatus);
}
