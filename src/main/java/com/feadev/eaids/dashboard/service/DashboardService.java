package com.feadev.eaids.dashboard.service;

import com.feadev.eaids.dashboard.dto.response.AthleteDashboardResponse;
import com.feadev.eaids.dashboard.dto.response.CoachDashboardResponse;

import java.util.UUID;

public interface DashboardService {

    AthleteDashboardResponse getAthleteDashboard(UUID athleteUserId);

    CoachDashboardResponse getCoachDashboard(UUID coachUserId);
}
