package com.ssasinsa.wearagain.domain.dashboard.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.dashboard.docs.DashboardApiDocs;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.dashboard.service.DashboardQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = DashboardApiDocs.TAG_NAME, description = DashboardApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardQueryService dashboardQueryService;

    @DashboardApiDocs.GetOverview
    @GetMapping("/overview")
    public ResponseEntity<DashboardSnapshotResponse> getOverview(
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        DashboardSnapshotResponse response = dashboardQueryService.getLatestSnapshot();
        return ResponseEntity.ok(response);
    }
}
