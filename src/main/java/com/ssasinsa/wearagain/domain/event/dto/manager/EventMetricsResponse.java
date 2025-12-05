package com.ssasinsa.wearagain.domain.event.dto.manager;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 지표 응답")
public record EventMetricsResponse(
        @Schema(description = "이벤트 지표 개수", example = "5")
        long count,
        @Schema(description = "이벤트 지표 목록")
        List<EventMetricResponse> items
) {

    public static EventMetricsResponse of(List<EventMetricResponse> items) {
        return new EventMetricsResponse(items.size(), List.copyOf(items));
    }
}
