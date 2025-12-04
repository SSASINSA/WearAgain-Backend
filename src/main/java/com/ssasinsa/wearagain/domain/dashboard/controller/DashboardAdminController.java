package com.ssasinsa.wearagain.domain.dashboard.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.dashboard.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardSnapshotResponse> getOverview(
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        DashboardSnapshotResponse response = dashboardQueryService.getLatestSnapshot();
        return ResponseEntity.ok(response);
    }
}
