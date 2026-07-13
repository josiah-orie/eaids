package com.feadev.eaids.scouting.service;

import com.feadev.eaids.scouting.dto.request.CreateInterviewRequest;
import com.feadev.eaids.scouting.dto.request.ReviewInterviewRequest;
import com.feadev.eaids.scouting.dto.response.InterviewRequestResponse;
import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InterviewRequestService {

    InterviewRequestResponse submitRequest(UUID scoutUserId, CreateInterviewRequest request);

    Page<InterviewRequestResponse> getRequestsByStatus(InterviewStatus status, Pageable pageable);

    InterviewRequestResponse reviewRequest(UUID requestId, ReviewInterviewRequest review);

    Page<InterviewRequestResponse> getScoutRequests(UUID scoutUserId, Pageable pageable);
}
