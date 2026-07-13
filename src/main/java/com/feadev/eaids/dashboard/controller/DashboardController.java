package com.feadev.eaids.dashboard.controller;

import com.feadev.eaids.dashboard.dto.response.AdminDashboardResponse;
import com.feadev.eaids.dashboard.dto.response.AthleteDashboardResponse;
import com.feadev.eaids.dashboard.dto.response.CoachDashboardResponse;
import com.feadev.eaids.dashboard.service.AdminDashboardService;
import com.feadev.eaids.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Aggregated dashboard data for athletes, coaches, and admins")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/athlete")
    @PreAuthorize("hasRole('ATHLETE')")
    @Operation(summary = "Get athlete dashboard", description = "Returns performance trends, rankings, development plan summary, upcoming camps, and recent evaluations")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    public ResponseEntity<AthleteDashboardResponse> getAthleteDashboard(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(dashboardService.getAthleteDashboard(userId));
    }

    @GetMapping("/coach")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Get coach dashboard", description = "Returns assigned athletes, evaluation activity, development plans summary, camp activity, and recent evaluations")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    public ResponseEntity<CoachDashboardResponse> getCoachDashboard(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(dashboardService.getCoachDashboard(userId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard", description = "Returns platform-wide aggregated metrics including user stats, camp stats, evaluations, selections, scoring, and development plans")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(
            @RequestParam(defaultValue = "30") int period) {
        return ResponseEntity.ok(adminDashboardService.getAdminDashboard(period));
    }
}
