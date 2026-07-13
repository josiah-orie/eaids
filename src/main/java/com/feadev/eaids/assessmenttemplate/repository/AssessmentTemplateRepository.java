package com.feadev.eaids.assessmenttemplate.repository;

import com.feadev.eaids.assessmenttemplate.model.AssessmentTemplate;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentTemplateRepository extends JpaRepository<AssessmentTemplate, UUID> {

    List<AssessmentTemplate> findByPositionProfileId(UUID positionProfileId);

    List<AssessmentTemplate> findByAgeCategory(AgeCategory ageCategory);

    List<AssessmentTemplate> findByIsActiveTrue();

    @Query("SELECT t FROM AssessmentTemplate t WHERE t.positionProfile.position = :position AND t.isActive = true")
    List<AssessmentTemplate> findActiveByPosition(@Param("position") FootballPosition position);

    @Query("SELECT t FROM AssessmentTemplate t WHERE t.positionProfile.position = :position AND t.ageCategory = :ageCategory AND t.isActive = true")
    List<AssessmentTemplate> findActiveByPositionAndAgeCategory(
            @Param("position") FootballPosition position,
            @Param("ageCategory") AgeCategory ageCategory);

    List<AssessmentTemplate> findByPositionProfileIdAndIsActiveTrue(UUID positionProfileId);
}
