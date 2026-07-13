package com.feadev.eaids.assessmenttemplate.service.impl;

import com.feadev.eaids.assessmenttemplate.dto.request.CreateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.request.TemplateMetricRequest;
import com.feadev.eaids.assessmenttemplate.dto.request.UpdateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.response.AssessmentTemplateResponse;
import com.feadev.eaids.assessmenttemplate.dto.response.TemplateMetricResponse;
import com.feadev.eaids.assessmenttemplate.model.AssessmentTemplate;
import com.feadev.eaids.assessmenttemplate.model.AssessmentTemplateMetric;
import com.feadev.eaids.assessmenttemplate.repository.AssessmentTemplateRepository;
import com.feadev.eaids.assessmenttemplate.service.AssessmentTemplateService;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.positionprofile.model.PositionProfile;
import com.feadev.eaids.positionprofile.repository.PositionProfileRepository;
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
public class AssessmentTemplateServiceImpl implements AssessmentTemplateService {

    private final AssessmentTemplateRepository templateRepository;
    private final PositionProfileRepository positionProfileRepository;

    @Override
    public AssessmentTemplateResponse createTemplate(CreateAssessmentTemplateRequest request) {
        PositionProfile profile = positionProfileRepository.findById(request.getPositionProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Position profile not found: " + request.getPositionProfileId()));

        if (!profile.isActive()) {
            throw new IllegalStateException("Cannot create template for an inactive position profile");
        }

        validateMetricWeights(request.getMetrics());

        AssessmentTemplate template = AssessmentTemplate.builder()
                .positionProfile(profile)
                .name(request.getName())
                .ageCategory(request.getAgeCategory())
                .passThreshold(request.getPassThreshold())
                .scoringScaleMax(request.getScoringScaleMax())
                .isActive(true)
                .build();

        List<AssessmentTemplateMetric> metrics = request.getMetrics().stream()
                .map(m -> buildMetric(m, template))
                .collect(Collectors.toList());
        template.setMetrics(metrics);

        AssessmentTemplate saved = templateRepository.save(template);
        log.info("Assessment template created: {} linked to profile {}", saved.getName(), profile.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentTemplateResponse getTemplate(UUID templateId) {
        AssessmentTemplate template = findOrThrow(templateId);
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentTemplateResponse> getAllTemplates(
            UUID positionProfileId, FootballPosition position, AgeCategory ageCategory) {

        List<AssessmentTemplate> templates;

        if (position != null && ageCategory != null) {
            templates = templateRepository.findActiveByPositionAndAgeCategory(position, ageCategory);
        } else if (position != null) {
            templates = templateRepository.findActiveByPosition(position);
        } else if (positionProfileId != null) {
            templates = templateRepository.findByPositionProfileId(positionProfileId);
        } else if (ageCategory != null) {
            templates = templateRepository.findByAgeCategory(ageCategory);
        } else {
            templates = templateRepository.findAll();
        }

        return templates.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AssessmentTemplateResponse updateTemplate(UUID templateId, UpdateAssessmentTemplateRequest request) {
        AssessmentTemplate template = findOrThrow(templateId);

        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getAgeCategory() != null) {
            template.setAgeCategory(request.getAgeCategory());
        }
        if (request.getPassThreshold() != null) {
            template.setPassThreshold(request.getPassThreshold());
        }
        if (request.getScoringScaleMax() != null) {
            template.setScoringScaleMax(request.getScoringScaleMax());
        }
        if (request.getMetrics() != null && !request.getMetrics().isEmpty()) {
            validateMetricWeights(request.getMetrics());
            template.getMetrics().clear();
            List<AssessmentTemplateMetric> newMetrics = request.getMetrics().stream()
                    .map(m -> buildMetric(m, template))
                    .collect(Collectors.toList());
            template.getMetrics().addAll(newMetrics);
        }

        AssessmentTemplate saved = templateRepository.save(template);
        log.info("Assessment template updated: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public void deleteTemplate(UUID templateId) {
        AssessmentTemplate template = findOrThrow(templateId);
        template.setActive(false);
        templateRepository.save(template);
        log.info("Assessment template deactivated: {}", templateId);
    }

    private AssessmentTemplate findOrThrow(UUID templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment template not found with id: " + templateId));
    }

    private void validateMetricWeights(List<TemplateMetricRequest> metrics) {
        BigDecimal sum = metrics.stream()
                .map(TemplateMetricRequest::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Metric weights must sum to 100. Current sum: " + sum);
        }
    }

    private AssessmentTemplateMetric buildMetric(TemplateMetricRequest req, AssessmentTemplate template) {
        return AssessmentTemplateMetric.builder()
                .template(template)
                .metricName(req.getMetricName())
                .category(req.getCategory())
                .unit(req.getUnit())
                .weight(req.getWeight())
                .maxScore(req.getMaxScore())
                .displayOrder(req.getDisplayOrder())
                .build();
    }

    private AssessmentTemplateResponse mapToResponse(AssessmentTemplate template) {
        List<TemplateMetricResponse> metricResponses = template.getMetrics().stream()
                .map(m -> TemplateMetricResponse.builder()
                        .id(m.getId())
                        .metricName(m.getMetricName())
                        .category(m.getCategory())
                        .unit(m.getUnit())
                        .weight(m.getWeight())
                        .maxScore(m.getMaxScore())
                        .displayOrder(m.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        return AssessmentTemplateResponse.builder()
                .id(template.getId())
                .positionProfileId(template.getPositionProfile().getId())
                .positionProfileName(template.getPositionProfile().getName())
                .position(template.getPositionProfile().getPosition().name())
                .name(template.getName())
                .ageCategory(template.getAgeCategory().name())
                .passThreshold(template.getPassThreshold())
                .scoringScaleMax(template.getScoringScaleMax())
                .active(template.isActive())
                .metrics(metricResponses)
                .createdDate(template.getCreatedDate())
                .lastModifiedDate(template.getLastModifiedDate())
                .build();
    }
}
