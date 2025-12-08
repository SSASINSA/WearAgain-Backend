package com.ssasinsa.wearagain.domain.report.controller;

import com.ssasinsa.wearagain.domain.report.docs.ReportApiDocs;
import com.ssasinsa.wearagain.domain.report.dto.ReportCreateResponse;
import com.ssasinsa.wearagain.domain.report.dto.ReportStatusResponse;
import com.ssasinsa.wearagain.domain.report.service.EventReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@Tag(name = ReportApiDocs.TAG_NAME, description = ReportApiDocs.TAG_DESCRIPTION)
public class EventReportController {

    private final EventReportService reportService;

    public EventReportController(EventReportService reportService) {
        this.reportService = reportService;
    }

    @ReportApiDocs.CreateEventReport
    @PostMapping
    public ResponseEntity<ReportCreateResponse> createReport(@RequestParam Long eventId) {
        ReportCreateResponse response = reportService.requestReport(eventId);
        return ResponseEntity.ok(response);
    }

    @ReportApiDocs.GetReportStatus
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportStatusResponse> getReportStatus(@PathVariable String reportId) {
        ReportStatusResponse response = reportService.getReportStatus(reportId);
        return ResponseEntity.ok(response);
    }
}
