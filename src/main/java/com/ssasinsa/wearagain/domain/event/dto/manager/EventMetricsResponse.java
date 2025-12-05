package com.ssasinsa.wearagain.domain.event.dto.manager;

import java.util.List;

public record EventMetricsResponse(
        long count,
        List<EventMetricResponse> items
) {

    public static EventMetricsResponse of(List<EventMetricResponse> items) {
        return new EventMetricsResponse(items.size(), items);
    }
}
