package com.ssasinsa.wearagain.domain.report.dto;

import com.ssasinsa.wearagain.domain.report.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리포트 상태 응답")
public record ReportStatusResponse(
        @Schema(description = "리포트 ID")
        String reportId,
        @Schema(description = "상태")
        ReportStatus status,
        @Schema(description = "다운로드 링크(READY일 때만 설정)")
        String downloadUrl,
        @Schema(description = "실패 사유(FAILED일 때 설정)")
        String message
) {
}
