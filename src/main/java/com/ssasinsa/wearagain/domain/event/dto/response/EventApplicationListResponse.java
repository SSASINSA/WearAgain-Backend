package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사용자 신청 내역 리스트 응답")
public record EventApplicationListResponse(
        @Schema(description = "신청 내역 목록")
        List<EventApplicationSummaryResponse> items,

        @Schema(description = "다음 페이지 조회용 커서", example = "MjAyNS0wMS0yOFQxMjozMDowMC4wMDBaOjEyMw==")
        String nextCursor,

        @Schema(description = "다음 페이지 여부", example = "true")
        boolean hasNext
) {
}
