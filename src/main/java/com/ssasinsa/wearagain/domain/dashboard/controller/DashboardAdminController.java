package com.ssasinsa.wearagain.domain.dashboard.controller;

import com.ssasinsa.wearagain.domain.dashboard.docs.DashboardApiDocs;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.dashboard.service.DashboardQueryService;
import com.ssasinsa.wearagain.domain.event.dto.manager.EventMetricResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.EventMetricsPeriod;
import com.ssasinsa.wearagain.domain.event.service.EventMetricsService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = DashboardApiDocs.TAG_NAME, description = DashboardApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardQueryService dashboardQueryService;
    private final EventMetricsService eventMetricsService;

    @DashboardApiDocs.GetOverview
    @GetMapping("/overview")
    public ResponseEntity<DashboardSnapshotResponse> getOverview() {
        DashboardSnapshotResponse response = dashboardQueryService.getLatestSnapshot();
        return ResponseEntity.ok(response);
    }

    @DashboardApiDocs.GetEventMetrics
    @GetMapping("/metrics")
    public ResponseEntity<List<EventMetricResponse>> getEventMetrics(
            @RequestParam(value = "period", required = false) String periodValue
    ) {
        EventMetricsPeriod period = EventMetricsPeriod.from(periodValue);
        LocalDate fromDate = period.fromDate(LocalDate.now());
        List<EventMetricResponse> response = eventMetricsService.getEventMetrics(fromDate);
        return ResponseEntity.ok(response);
    }
}
