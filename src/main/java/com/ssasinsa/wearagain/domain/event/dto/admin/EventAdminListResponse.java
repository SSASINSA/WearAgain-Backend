package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 행사 목록 응답")
public record EventAdminListResponse(
        @Schema(description = "행사 목록")
        List<EventAdminSummaryResponse> events,

        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "총 데이터 건수", example = "42")
        long totalElements,

        @Schema(description = "총 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
