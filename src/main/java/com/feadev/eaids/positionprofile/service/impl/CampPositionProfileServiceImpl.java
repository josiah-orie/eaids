package com.feadev.eaids.positionprofile.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.positionprofile.dto.response.CampPositionProfileResponse;
import com.feadev.eaids.positionprofile.model.CampPositionProfile;
import com.feadev.eaids.positionprofile.model.PositionProfile;
import com.feadev.eaids.positionprofile.repository.CampPositionProfileRepository;
import com.feadev.eaids.positionprofile.repository.PositionProfileRepository;
import com.feadev.eaids.positionprofile.service.CampPositionProfileService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampPositionProfileServiceImpl implements CampPositionProfileService {

    private final CampPositionProfileRepository campPositionProfileRepository;
    private final CampRepository campRepository;
    private final PositionProfileRepository positionProfileRepository;

    @Override
    public CampPositionProfileResponse assignProfile(UUID campId, UUID positionProfileId) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        PositionProfile profile = positionProfileRepository.findById(positionProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Position profile not found: " + positionProfileId));

        if (!profile.isActive()) {
            throw new IllegalStateException("Cannot assign an inactive position profile");
        }

        if (campPositionProfileRepository.existsByCampIdAndPositionProfileId(campId, positionProfileId)) {
            throw new IllegalStateException("Position profile is already assigned to this camp");
        }

        CampPositionProfile cpp = CampPositionProfile.builder()
                .camp(camp)
                .positionProfile(profile)
                .build();

        CampPositionProfile saved = campPositionProfileRepository.save(cpp);
        log.info("Position profile {} assigned to camp {}", positionProfileId, campId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampPositionProfileResponse> getCampProfiles(UUID campId) {
        if (!campRepository.existsById(campId)) {
            throw new ResourceNotFoundException("Camp not found: " + campId);
        }
        return campPositionProfileRepository.findByCampId(campId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void unassignProfile(UUID campId, UUID positionProfileId) {
        CampPositionProfile cpp = campPositionProfileRepository.findByCampIdAndPositionProfileId(campId, positionProfileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Position profile assignment not found for camp: " + campId + " profile: " + positionProfileId));
        campPositionProfileRepository.delete(cpp);
        log.info("Position profile {} unassigned from camp {}", positionProfileId, campId);
    }

    private CampPositionProfileResponse mapToResponse(CampPositionProfile cpp) {
        return CampPositionProfileResponse.builder()
                .id(cpp.getId())
                .campId(cpp.getCamp().getId())
                .positionProfileId(cpp.getPositionProfile().getId())
                .positionProfileName(cpp.getPositionProfile().getName())
                .position(cpp.getPositionProfile().getPosition().name())
                .assignedDate(cpp.getAssignedDate())
                .build();
    }
}
