package com.feadev.eaids.positionprofile.service.impl;

import com.feadev.eaids.positionprofile.dto.request.CreatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.request.ScoringMetricRequest;
import com.feadev.eaids.positionprofile.dto.request.UpdatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.request.WeightTemplateRequest;
import com.feadev.eaids.positionprofile.dto.response.PositionProfileResponse;
import com.feadev.eaids.positionprofile.dto.response.ScoringMetricResponse;
import com.feadev.eaids.positionprofile.dto.response.WeightTemplateResponse;
import com.feadev.eaids.positionprofile.model.PositionProfile;
import com.feadev.eaids.positionprofile.model.PositionScoringMetric;
import com.feadev.eaids.positionprofile.model.PositionWeightTemplate;
import com.feadev.eaids.positionprofile.repository.PositionProfileRepository;
import com.feadev.eaids.positionprofile.service.PositionProfileService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PositionProfileServiceImpl implements PositionProfileService {

    private final PositionProfileRepository profileRepository;

    @Override
    public PositionProfileResponse createProfile(CreatePositionProfileRequest request) {
        if (profileRepository.existsByPositionAndIsActiveTrue(request.getPosition())) {
            throw new IllegalStateException(
                    "An active position profile already exists for position: " + request.getPosition());
        }

        validateWeightSum(request.getWeightTemplate());

        PositionProfile profile = PositionProfile.builder()
                .position(request.getPosition())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        // Set up metrics
        List<PositionScoringMetric> metrics = request.getMetrics().stream()
                .map(m -> buildMetric(m, profile))
                .collect(Collectors.toList());
        profile.setMetrics(metrics);

        // Set up weight template
        PositionWeightTemplate wt = buildWeightTemplate(request.getWeightTemplate(), profile);
        profile.setWeightTemplate(wt);

        PositionProfile saved = profileRepository.save(profile);
        log.info("Position profile created: {} for position {}", saved.getName(), saved.getPosition());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionProfileResponse getProfile(UUID profileId) {
        PositionProfile profile = findOrThrow(profileId);
        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionProfileResponse> getAllProfiles(FootballPosition position, Boolean active) {
        List<PositionProfile> profiles;
        if (position != null && Boolean.TRUE.equals(active)) {
            profiles = profileRepository.findByPositionAndIsActiveTrue(position);
        } else if (position != null) {
            profiles = profileRepository.findByPosition(position);
        } else if (Boolean.TRUE.equals(active)) {
            profiles = profileRepository.findByIsActiveTrue();
        } else {
            profiles = profileRepository.findAll();
        }
        return profiles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PositionProfileResponse updateProfile(UUID profileId, UpdatePositionProfileRequest request) {
        PositionProfile profile = findOrThrow(profileId);

        if (request.getName() != null) {
            profile.setName(request.getName());
        }
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }
        if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
            profile.getMetrics().clear();
            List<PositionScoringMetric> newMetrics = request.getMetrics().stream()
                    .map(m -> buildMetric(m, profile))
                    .collect(Collectors.toList());
            profile.getMetrics().addAll(newMetrics);
        }
        if (request.getWeightTemplate() != null) {
            validateWeightSum(request.getWeightTemplate());
            if (profile.getWeightTemplate() != null) {
                updateWeightTemplate(profile.getWeightTemplate(), request.getWeightTemplate());
            } else {
                profile.setWeightTemplate(buildWeightTemplate(request.getWeightTemplate(), profile));
            }
        }

        PositionProfile saved = profileRepository.save(profile);
        log.info("Position profile updated: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public void deleteProfile(UUID profileId) {
        PositionProfile profile = findOrThrow(profileId);
        profile.setActive(false);
        profileRepository.save(profile);
        log.info("Position profile deactivated: {}", profileId);
    }

    private PositionProfile findOrThrow(UUID profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Position profile not found with id: " + profileId));
    }

    private void validateWeightSum(WeightTemplateRequest wt) {
        BigDecimal sum = wt.getPhysicalWeight()
                .add(wt.getTechnicalWeight())
                .add(wt.getTacticalWeight())
                .add(wt.getAttendanceWeight())
                .add(wt.getDisciplineWeight())
                .add(wt.getCoachReviewWeight())
                .add(wt.getMatchStatsWeight());
        if (sum.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Weight template values must sum to 100. Current sum: " + sum);
        }
    }

    private PositionScoringMetric buildMetric(ScoringMetricRequest req, PositionProfile profile) {
        return PositionScoringMetric.builder()
                .positionProfile(profile)
                .metricName(req.getMetricName())
                .maxScore(req.getMaxScore())
                .weight(req.getWeight())
                .category(req.getCategory())
                .description(req.getDescription())
                .displayOrder(req.getDisplayOrder())
                .build();
    }

    private PositionWeightTemplate buildWeightTemplate(WeightTemplateRequest req, PositionProfile profile) {
        return PositionWeightTemplate.builder()
                .positionProfile(profile)
                .physicalWeight(req.getPhysicalWeight())
                .technicalWeight(req.getTechnicalWeight())
                .tacticalWeight(req.getTacticalWeight())
                .attendanceWeight(req.getAttendanceWeight())
                .disciplineWeight(req.getDisciplineWeight())
                .coachReviewWeight(req.getCoachReviewWeight())
                .matchStatsWeight(req.getMatchStatsWeight())
                .build();
    }

    private void updateWeightTemplate(PositionWeightTemplate existing, WeightTemplateRequest req) {
        existing.setPhysicalWeight(req.getPhysicalWeight());
        existing.setTechnicalWeight(req.getTechnicalWeight());
        existing.setTacticalWeight(req.getTacticalWeight());
        existing.setAttendanceWeight(req.getAttendanceWeight());
        existing.setDisciplineWeight(req.getDisciplineWeight());
        existing.setCoachReviewWeight(req.getCoachReviewWeight());
        existing.setMatchStatsWeight(req.getMatchStatsWeight());
    }

    private PositionProfileResponse mapToResponse(PositionProfile profile) {
        List<ScoringMetricResponse> metricResponses = profile.getMetrics().stream()
                .map(m -> ScoringMetricResponse.builder()
                        .id(m.getId())
                        .metricName(m.getMetricName())
                        .maxScore(m.getMaxScore())
                        .weight(m.getWeight())
                        .category(m.getCategory())
                        .description(m.getDescription())
                        .displayOrder(m.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        WeightTemplateResponse wtResponse = null;
        if (profile.getWeightTemplate() != null) {
            PositionWeightTemplate wt = profile.getWeightTemplate();
            wtResponse = WeightTemplateResponse.builder()
                    .id(wt.getId())
                    .physicalWeight(wt.getPhysicalWeight())
                    .technicalWeight(wt.getTechnicalWeight())
                    .tacticalWeight(wt.getTacticalWeight())
                    .attendanceWeight(wt.getAttendanceWeight())
                    .disciplineWeight(wt.getDisciplineWeight())
                    .coachReviewWeight(wt.getCoachReviewWeight())
                    .matchStatsWeight(wt.getMatchStatsWeight())
                    .build();
        }

        return PositionProfileResponse.builder()
                .id(profile.getId())
                .position(profile.getPosition().name())
                .name(profile.getName())
                .description(profile.getDescription())
                .active(profile.isActive())
                .metrics(metricResponses)
                .weightTemplate(wtResponse)
                .createdDate(profile.getCreatedDate())
                .lastModifiedDate(profile.getLastModifiedDate())
                .build();
    }
}
