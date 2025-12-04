package com.ssasinsa.wearagain.domain.dashboard.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.dashboard.service.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Dashboard", description = "대시보드 집계 조회 API")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardQueryService dashboardQueryService;

    @Operation(summary = "대시보드 지표 조회", description = "배치로 집계된 최신 대시보드 스냅샷을 반환합니다.")
    @GetMapping("/overview")
    public ResponseEntity<DashboardSnapshotResponse> getOverview(
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        DashboardSnapshotResponse response = dashboardQueryService.getLatestSnapshot();
        return ResponseEntity.ok(response);
    }
}
