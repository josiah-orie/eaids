package com.feadev.eaids.analytics.benchmarking.service.impl;

import com.feadev.eaids.analytics.benchmarking.dto.request.CreateBenchmarkProfileRequest;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkComparisonResponse;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkProfileResponse;
import com.feadev.eaids.analytics.benchmarking.event.BenchmarkCapturedEvent;
import com.feadev.eaids.analytics.benchmarking.model.BenchmarkComparison;
import com.feadev.eaids.analytics.benchmarking.model.BenchmarkProfile;
import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.analytics.benchmarking.repository.BenchmarkComparisonRepository;
import com.feadev.eaids.analytics.benchmarking.repository.BenchmarkProfileRepository;
import com.feadev.eaids.analytics.benchmarking.service.BenchmarkingService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenchmarkingServiceImpl implements BenchmarkingService {

    private final BenchmarkProfileRepository benchmarkProfileRepository;
    private final BenchmarkComparisonRepository benchmarkComparisonRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BenchmarkProfileResponse createBenchmarkProfile(CreateBenchmarkProfileRequest request) {
        BenchmarkProfile profile = BenchmarkProfile.builder()
                .name(request.getName())
                .benchmarkType(request.getBenchmarkType())
                .position(request.getPosition())
                .ageCategory(request.getAgeCategory())
                .gender(request.getGender())
                .physicalBenchmark(request.getPhysicalBenchmark())
                .technicalBenchmark(request.getTechnicalBenchmark())
                .tacticalBenchmark(request.getTacticalBenchmark())
                .overallBenchmark(request.getOverallBenchmark())
                .sourceDescription(request.getSourceDescription())
                .autoCaptured(false)
                .build();

        BenchmarkProfile saved = benchmarkProfileRepository.save(profile);
        log.info("Benchmark profile created: id={}, name={}, type={}", saved.getId(), saved.getName(), saved.getBenchmarkType());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenchmarkProfileResponse> getAllBenchmarkProfiles() {
        return benchmarkProfileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BenchmarkProfileResponse getBenchmarkProfile(UUID id) {
        BenchmarkProfile profile = benchmarkProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Benchmark profile not found: " + id));
        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public BenchmarkProfileResponse updateBenchmarkProfile(UUID id, CreateBenchmarkProfileRequest request) {
        BenchmarkProfile profile = benchmarkProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Benchmark profile not found: " + id));

        profile.setName(request.getName());
        profile.setBenchmarkType(request.getBenchmarkType());
        profile.setPosition(request.getPosition());
        profile.setAgeCategory(request.getAgeCategory());
        profile.setGender(request.getGender());
        profile.setPhysicalBenchmark(request.getPhysicalBenchmark());
        profile.setTechnicalBenchmark(request.getTechnicalBenchmark());
        profile.setTacticalBenchmark(request.getTacticalBenchmark());
        profile.setOverallBenchmark(request.getOverallBenchmark());
        profile.setSourceDescription(request.getSourceDescription());

        BenchmarkProfile saved = benchmarkProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBenchmarkProfile(UUID id) {
        if (!benchmarkProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Benchmark profile not found: " + id);
        }
        benchmarkProfileRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BenchmarkComparisonResponse compareAgainstBenchmark(UUID enrollmentId, BenchmarkType type, FootballPosition position) {
        AthleteScore score = athleteScoreRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No scores found for enrollment: " + enrollmentId));

        // Find matching benchmark profiles
        List<BenchmarkProfile> benchmarks = position != null
                ? benchmarkProfileRepository.findByBenchmarkTypeAndPosition(type, position)
                : benchmarkProfileRepository.findByBenchmarkType(type);

        if (benchmarks.isEmpty()) {
            throw new ResourceNotFoundException("No benchmark profiles found for type: " + type
                    + (position != null ? " and position: " + position : ""));
        }

        // Use the first matching benchmark (could be enhanced to average multiple)
        BenchmarkProfile benchmark = benchmarks.get(0);

        // Calculate gaps (positive = athlete is above benchmark, negative = below)
        BigDecimal physicalGap = calculateGap(score.getPhysicalScore(), benchmark.getPhysicalBenchmark());
        BigDecimal technicalGap = calculateGap(score.getTechnicalScore(), benchmark.getTechnicalBenchmark());
        BigDecimal tacticalGap = calculateGap(score.getTacticalScore(), benchmark.getTacticalBenchmark());
        BigDecimal overallGap = calculateGap(score.getTotalWeightedScore(), benchmark.getOverallBenchmark());

        // Calculate percentile rank (athlete score / benchmark score * 100)
        BigDecimal percentileRank = BigDecimal.ZERO;
        if (benchmark.getOverallBenchmark() != null && benchmark.getOverallBenchmark().compareTo(BigDecimal.ZERO) > 0) {
            percentileRank = score.getTotalWeightedScore() != null
                    ? score.getTotalWeightedScore()
                    .divide(benchmark.getOverallBenchmark(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        BenchmarkComparison comparison = BenchmarkComparison.builder()
                .enrollmentId(enrollmentId)
                .benchmarkProfileId(benchmark.getId())
                .physicalGap(physicalGap)
                .technicalGap(technicalGap)
                .tacticalGap(tacticalGap)
                .overallGap(overallGap)
                .percentileRank(percentileRank)
                .comparedAt(LocalDateTime.now())
                .build();

        BenchmarkComparison saved = benchmarkComparisonRepository.save(comparison);

        return BenchmarkComparisonResponse.builder()
                .id(saved.getId())
                .enrollmentId(saved.getEnrollmentId())
                .benchmarkProfileId(saved.getBenchmarkProfileId())
                .benchmarkName(benchmark.getName())
                .physicalGap(saved.getPhysicalGap())
                .technicalGap(saved.getTechnicalGap())
                .tacticalGap(saved.getTacticalGap())
                .overallGap(saved.getOverallGap())
                .percentileRank(saved.getPercentileRank())
                .comparedAt(saved.getComparedAt())
                .build();
    }

    @Override
    @Transactional
    public void autoCaptureFromEliteSelection(UUID enrollmentId) {
        // Don't duplicate if already captured
        if (benchmarkProfileRepository.existsByCapturedFromEnrollmentId(enrollmentId)) {
            log.info("Benchmark already captured for enrollment: {}", enrollmentId);
            return;
        }

        AthleteScore score = athleteScoreRepository.findByEnrollmentId(enrollmentId).orElse(null);
        if (score == null) {
            log.warn("Cannot auto-capture benchmark: no scores for enrollment {}", enrollmentId);
            return;
        }

        BenchmarkProfile profile = BenchmarkProfile.builder()
                .name("Elite Champion - " + score.getPosition() + " - " + LocalDateTime.now().toLocalDate())
                .benchmarkType(BenchmarkType.HISTORICAL_CHAMPION)
                .position(score.getPosition())
                .physicalBenchmark(score.getPhysicalScore())
                .technicalBenchmark(score.getTechnicalScore())
                .tacticalBenchmark(score.getTacticalScore())
                .overallBenchmark(score.getTotalWeightedScore())
                .sourceDescription("Auto-captured from TIER_1_ELITE selection")
                .autoCaptured(true)
                .capturedFromEnrollmentId(enrollmentId)
                .build();

        BenchmarkProfile saved = benchmarkProfileRepository.save(profile);

        eventPublisher.publishEvent(new BenchmarkCapturedEvent(
                saved.getId(), enrollmentId, score.getPosition()));

        log.info("Auto-captured benchmark from TIER_1_ELITE: enrollmentId={}, position={}",
                enrollmentId, score.getPosition());
    }

    private BigDecimal calculateGap(BigDecimal athleteScore, BigDecimal benchmarkScore) {
        if (athleteScore == null || benchmarkScore == null) {
            return BigDecimal.ZERO;
        }
        return athleteScore.subtract(benchmarkScore).setScale(2, RoundingMode.HALF_UP);
    }

    private BenchmarkProfileResponse mapToResponse(BenchmarkProfile profile) {
        return BenchmarkProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .benchmarkType(profile.getBenchmarkType())
                .position(profile.getPosition())
                .ageCategory(profile.getAgeCategory())
                .gender(profile.getGender())
                .physicalBenchmark(profile.getPhysicalBenchmark())
                .technicalBenchmark(profile.getTechnicalBenchmark())
                .tacticalBenchmark(profile.getTacticalBenchmark())
                .overallBenchmark(profile.getOverallBenchmark())
                .sourceDescription(profile.getSourceDescription())
                .autoCaptured(profile.isAutoCaptured())
                .createdDate(profile.getCreatedDate())
                .build();
    }
}
