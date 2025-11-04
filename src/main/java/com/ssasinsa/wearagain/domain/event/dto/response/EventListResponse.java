package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사용자 행사 목록 응답")
public record EventListResponse(
        @Schema(description = "행사 목록")
        List<EventSummaryResponse> events,

        @Schema(description = "다음 페이지 요청에 사용할 커서", example = "105", nullable = true)
        String nextCursor,

        @Schema(description = "다음 데이터 존재 여부", example = "true")
        boolean hasNext
) {
}
