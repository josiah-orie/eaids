package com.feadev.eaids.dashboard.service;

import com.feadev.eaids.dashboard.dto.response.AdminDashboardResponse;

public interface AdminDashboardService {

    AdminDashboardResponse getAdminDashboard(int periodDays);
}
