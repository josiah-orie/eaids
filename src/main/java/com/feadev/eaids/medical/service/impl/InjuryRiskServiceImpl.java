package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.medical.dto.response.InjuryRiskResponse;
import com.feadev.eaids.medical.model.AthleteInjury;
import com.feadev.eaids.medical.model.AthleteMedicalProfile;
import com.feadev.eaids.medical.repository.AthleteInjuryRepository;
import com.feadev.eaids.medical.repository.AthleteMedicalProfileRepository;
import com.feadev.eaids.medical.risk.InjuryRiskEngine;
import com.feadev.eaids.medical.risk.InjuryRiskResult;
import com.feadev.eaids.medical.service.InjuryRiskService;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.feadev.eaids.config.CacheConfig.INJURY_RISK_PROFILES_CACHE;

@Service("medicalInjuryRiskService")
@RequiredArgsConstructor
@Slf4j
public class InjuryRiskServiceImpl implements InjuryRiskService {

    private final AthleteInjuryRepository injuryRepository;
    private final AthleteMedicalProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final InjuryRiskEngine riskEngine;

    @Override
    @Cacheable(value = INJURY_RISK_PROFILES_CACHE, key = "#athleteUserId")
    @Transactional(readOnly = true)
    public InjuryRiskResponse getRiskProfile(UUID athleteUserId) {
        log.debug("Computing injury risk for athlete {} (cache miss)", athleteUserId);

        List<AthleteInjury> injuries = injuryRepository.findByAthleteUserId(athleteUserId);

        AthleteMedicalProfile profile = profileRepository
                .findByAthleteUserId(athleteUserId)
                .orElse(null);

        InjuryRiskResult result = riskEngine.compute(athleteUserId, injuries, profile);

        String athleteName = userRepository.findById(athleteUserId)
                .map(User::getEmail)
                .orElse("Unknown");

        return InjuryRiskResponse.builder()
                .athleteUserId(athleteUserId)
                .athleteName(athleteName)
                .riskLevel(result.riskLevel())
                .flags(result.flags())
                .summary(result.summary())
                .computedAt(result.computedAt())
                .build();
    }

    @Override
    @CacheEvict(value = INJURY_RISK_PROFILES_CACHE, key = "#athleteUserId")
    public void evictRiskCache(UUID athleteUserId) {
        log.debug("Injury risk cache evicted for athlete {}", athleteUserId);
        // Method body intentionally empty — the @CacheEvict annotation does all the work.
    }
}
