package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.AssignStaffRequest;
import com.feadev.eaids.academy.dto.response.ScheduleStaffResponse;

import java.util.List;
import java.util.UUID;

/**
 * Admin operations for assigning coaches and scouts to training schedules.
 */
public interface AcademyScheduleStaffService {

    ScheduleStaffResponse assignStaff(UUID scheduleId, AssignStaffRequest request);

    void removeStaff(UUID scheduleId, UUID staffAssignmentId);

    List<ScheduleStaffResponse> listStaff(UUID scheduleId);
}
