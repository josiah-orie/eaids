package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.AddressDto;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.dto.response.TrainingScheduleResponse;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;

/**
 * Shared mapping helpers between academy entities and their API responses.
 */
public final class AcademyMapper {

    private AcademyMapper() {
    }

    public static IndividualApplicationResponse toResponse(AcademyIndividualApplication entity) {
        return IndividualApplicationResponse.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .age(entity.getAge())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .location(entity.getLocation() != null ? entity.getLocation().name() : null)
                .address(AddressDto.from(entity.getAddress()))
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .reviewNotes(entity.getReviewNotes())
                .reviewedBy(entity.getReviewedBy())
                .reviewDate(entity.getReviewDate())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static InstitutionApplicationResponse toResponse(AcademyInstitutionApplication entity) {
        return InstitutionApplicationResponse.builder()
                .id(entity.getId())
                .institutionType(entity.getInstitutionType() != null
                        ? entity.getInstitutionType().name() : null)
                .name(entity.getName())
                .contactEmail(entity.getContactEmail())
                .contactPhone(entity.getContactPhone())
                .address(AddressDto.from(entity.getAddress()))
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .reviewNotes(entity.getReviewNotes())
                .reviewedBy(entity.getReviewedBy())
                .reviewDate(entity.getReviewDate())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    public static TrainingScheduleResponse toResponse(AcademyTrainingSchedule entity) {
        return TrainingScheduleResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .ageCategory(entity.getAgeCategory() != null ? entity.getAgeCategory().name() : null)
                .targetType(entity.getTargetType() != null ? entity.getTargetType().name() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .capacity(entity.getCapacity())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}
