package com.feadev.eaids.assessmenttemplate.service;

import com.feadev.eaids.assessmenttemplate.dto.request.CreateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.request.UpdateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.response.AssessmentTemplateResponse;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.technical.model.enums.FootballPosition;

import java.util.List;
import java.util.UUID;

public interface AssessmentTemplateService {

    AssessmentTemplateResponse createTemplate(CreateAssessmentTemplateRequest request);

    AssessmentTemplateResponse getTemplate(UUID templateId);

    List<AssessmentTemplateResponse> getAllTemplates(UUID positionProfileId, FootballPosition position, AgeCategory ageCategory);

    AssessmentTemplateResponse updateTemplate(UUID templateId, UpdateAssessmentTemplateRequest request);

    void deleteTemplate(UUID templateId);
}
