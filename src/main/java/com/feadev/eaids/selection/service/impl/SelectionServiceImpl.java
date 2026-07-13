package com.feadev.eaids.selection.service.impl;

import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.model.enums.RankingType;
import com.feadev.eaids.ranking.repository.AthleteRankingRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.selection.dto.request.ApproveSelectionRequest;
import com.feadev.eaids.selection.dto.response.SelectionResponse;
import com.feadev.eaids.selection.event.SelectionApprovedEvent;
import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import com.feadev.eaids.selection.model.enums.SelectionTier;
import com.feadev.eaids.selection.repository.SelectionDecisionRepository;
import com.feadev.eaids.selection.service.SelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SelectionServiceImpl implements SelectionService {

    private final SelectionDecisionRepository selectionRepository;
    private final AthleteRankingRepository rankingRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<SelectionResponse> generateSelections(UUID campId) {
        List<AthleteRanking> overallRankings = rankingRepository.findByCampIdAndRankingType(campId, RankingType.OVERALL);

        if (overallRankings.isEmpty()) {
            log.warn("No overall rankings found for camp {}. Cannot generate selections.", campId);
            return List.of();
        }

        List<SelectionDecision> decisions = new ArrayList<>();

        for (AthleteRanking ranking : overallRankings) {
            SelectionTier recommendedTier = determineTier(ranking.getPercentile());

            SelectionDecision decision = selectionRepository.findByEnrollmentId(ranking.getEnrollment().getId())
                    .orElse(new SelectionDecision());

            decision.setEnrollment(ranking.getEnrollment());
            decision.setCampId(campId);
            decision.setRecommendedTier(recommendedTier);
            decision.setApprovalStatus(ApprovalStatus.PENDING);

            decisions.add(decision);
        }

        List<SelectionDecision> saved = selectionRepository.saveAll(decisions);
        log.info("Selection decisions generated for camp {}. Total: {}", campId, saved.size());

        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SelectionResponse> getSelections(UUID campId, ApprovalStatus status) {
        List<SelectionDecision> decisions;
        if (status != null) {
            decisions = selectionRepository.findByCampIdAndApprovalStatus(campId, status);
        } else {
            decisions = selectionRepository.findByCampId(campId);
        }
        return decisions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public SelectionResponse approveSelection(UUID selectionId, ApproveSelectionRequest request, UUID reviewerId) {
        SelectionDecision decision = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Selection decision not found: " + selectionId));

        decision.setFinalTier(request.getFinalTier());
        decision.setApprovalStatus(ApprovalStatus.APPROVED);
        decision.setReviewedBy(reviewerId);
        decision.setComments(request.getComments());
        decision.setApprovalDate(LocalDateTime.now());

        SelectionDecision saved = selectionRepository.save(decision);

        eventPublisher.publishEvent(new SelectionApprovedEvent(
                saved.getId(), saved.getCampId(), saved.getEnrollment().getId()));
        log.info("Selection {} approved by reviewer {}", selectionId, reviewerId);

        return mapToResponse(saved);
    }

    @Override
    public SelectionResponse rejectSelection(UUID selectionId, String comments, UUID reviewerId) {
        SelectionDecision decision = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Selection decision not found: " + selectionId));

        decision.setApprovalStatus(ApprovalStatus.REJECTED);
        decision.setReviewedBy(reviewerId);
        decision.setComments(comments);

        SelectionDecision saved = selectionRepository.save(decision);
        log.info("Selection {} rejected by reviewer {}", selectionId, reviewerId);

        return mapToResponse(saved);
    }

    private SelectionTier determineTier(BigDecimal percentile) {
        if (percentile.compareTo(new BigDecimal("90.00")) >= 0) {
            return SelectionTier.TIER_1_ELITE;
        } else if (percentile.compareTo(new BigDecimal("75.00")) >= 0) {
            return SelectionTier.TIER_2_ADVANCED;
        } else if (percentile.compareTo(new BigDecimal("50.00")) >= 0) {
            return SelectionTier.TIER_3_DEVELOPING;
        } else if (percentile.compareTo(new BigDecimal("25.00")) >= 0) {
            return SelectionTier.TIER_4_POTENTIAL;
        } else {
            return SelectionTier.TIER_5_NOT_SELECTED;
        }
    }

    private SelectionResponse mapToResponse(SelectionDecision decision) {
        String athleteName = "Unknown";
        if (decision.getEnrollment() != null && decision.getEnrollment().getUser() != null) {
            athleteName = athleteProfileRepository.findByUser_Id(decision.getEnrollment().getUser().getId())
                    .map(profile -> profile.getFullName())
                    .orElse("Unknown");
        }

        return SelectionResponse.builder()
                .id(decision.getId())
                .enrollmentId(decision.getEnrollment().getId())
                .athleteName(athleteName)
                .campId(decision.getCampId())
                .recommendedTier(decision.getRecommendedTier())
                .finalTier(decision.getFinalTier())
                .approvalStatus(decision.getApprovalStatus())
                .reviewedBy(decision.getReviewedBy())
                .comments(decision.getComments())
                .approvalDate(decision.getApprovalDate())
                .createdDate(decision.getCreatedDate())
                .lastModifiedDate(decision.getLastModifiedDate())
                .build();
    }
}
