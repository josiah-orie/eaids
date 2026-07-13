package com.feadev.eaids.camp.service.impl;

import com.feadev.eaids.camp.dto.request.CreateCampRequest;
import com.feadev.eaids.camp.dto.request.UpdateCampRequest;
import com.feadev.eaids.camp.dto.response.CampResponse;
import com.feadev.eaids.camp.event.CampStatusChangedEvent;
import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.camp.service.CampService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampServiceImpl implements CampService {

    private final CampRepository campRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Valid status transitions for camp lifecycle.
     */
    private static final Map<CampStatus, Set<CampStatus>> VALID_TRANSITIONS = Map.of(
            CampStatus.DRAFT, Set.of(CampStatus.REGISTRATION_OPEN),
            CampStatus.REGISTRATION_OPEN, Set.of(CampStatus.REGISTRATION_CLOSED),
            CampStatus.REGISTRATION_CLOSED, Set.of(CampStatus.IN_PROGRESS),
            CampStatus.IN_PROGRESS, Set.of(CampStatus.COMPLETED),
            CampStatus.COMPLETED, Set.of(CampStatus.ARCHIVED),
            CampStatus.ARCHIVED, Set.of()
    );

    @Override
    public CampResponse createCamp(CreateCampRequest request) {
        Camp camp = Camp.builder()
                .name(request.getName())
                .description(request.getDescription())
                .campType(request.getCampType())
                .status(CampStatus.DRAFT)
                .ageCategory(request.getAgeCategory())
                .location(request.getLocation())
                .country(request.getCountry())
                .state(request.getState())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .capacity(request.getCapacity())
                .currentEnrollment(0)
                .aggregationStrategy(request.getAggregationStrategy() != null
                        ? request.getAggregationStrategy()
                        : com.feadev.eaids.camp.model.enums.AggregationStrategy.AVERAGE)
                .build();

        Camp saved = campRepository.save(camp);
        log.info("Camp created: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CampResponse getCamp(UUID campId) {
        Camp camp = findCampOrThrow(campId);
        return mapToResponse(camp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampResponse> getAllCamps() {
        return campRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampResponse> getCampsByStatus(CampStatus status) {
        return campRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampResponse> getCampsByType(CampType type) {
        return campRepository.findByCampType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CampResponse updateCamp(UUID campId, UpdateCampRequest request) {
        Camp camp = findCampOrThrow(campId);

        if (camp.getStatus() != CampStatus.DRAFT) {
            throw new IllegalStateException("Camp can only be updated in DRAFT status");
        }

        if (request.getName() != null) {
            camp.setName(request.getName());
        }
        if (request.getDescription() != null) {
            camp.setDescription(request.getDescription());
        }
        if (request.getCampType() != null) {
            camp.setCampType(request.getCampType());
        }
        if (request.getAgeCategory() != null) {
            camp.setAgeCategory(request.getAgeCategory());
        }
        if (request.getLocation() != null) {
            camp.setLocation(request.getLocation());
        }
        if (request.getCountry() != null) {
            camp.setCountry(request.getCountry());
        }
        if (request.getState() != null) {
            camp.setState(request.getState());
        }
        if (request.getStartDate() != null) {
            camp.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            camp.setEndDate(request.getEndDate());
        }
        if (request.getRegistrationDeadline() != null) {
            camp.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getCapacity() != null) {
            camp.setCapacity(request.getCapacity());
        }
        if (request.getAggregationStrategy() != null) {
            camp.setAggregationStrategy(request.getAggregationStrategy());
        }

        Camp saved = campRepository.save(camp);
        log.info("Camp updated: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public void deleteCamp(UUID campId) {
        Camp camp = findCampOrThrow(campId);

        if (camp.getStatus() != CampStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT camps can be deleted");
        }

        campRepository.delete(camp);
        log.info("Camp deleted: {}", campId);
    }

    @Override
    public CampResponse transitionStatus(UUID campId, CampStatus newStatus) {
        Camp camp = findCampOrThrow(campId);
        CampStatus currentStatus = camp.getStatus();

        Set<CampStatus> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition camp from %s to %s. Allowed transitions: %s",
                            currentStatus, newStatus, allowedTransitions));
        }

        camp.setStatus(newStatus);
        Camp saved = campRepository.save(camp);

        eventPublisher.publishEvent(new CampStatusChangedEvent(saved.getId(), currentStatus, newStatus));
        log.info("Camp {} transitioned from {} to {}", campId, currentStatus, newStatus);

        return mapToResponse(saved);
    }

    private Camp findCampOrThrow(UUID campId) {
        return campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found with id: " + campId));
    }

    private CampResponse mapToResponse(Camp camp) {
        return CampResponse.builder()
                .id(camp.getId())
                .name(camp.getName())
                .description(camp.getDescription())
                .campType(camp.getCampType().name())
                .status(camp.getStatus().name())
                .ageCategory(camp.getAgeCategory().name())
                .location(camp.getLocation())
                .country(camp.getCountry())
                .state(camp.getState())
                .startDate(camp.getStartDate())
                .endDate(camp.getEndDate())
                .registrationDeadline(camp.getRegistrationDeadline())
                .capacity(camp.getCapacity())
                .currentEnrollment(camp.getCurrentEnrollment())
                .aggregationStrategy(camp.getAggregationStrategy().name())
                .createdDate(camp.getCreatedDate())
                .lastModifiedDate(camp.getLastModifiedDate())
                .createdBy(camp.getCreatedBy())
                .lastModifiedBy(camp.getLastModifiedBy())
                .build();
    }
}
