package com.ssasinsa.wearagain.domain.event.dto.manager;

import java.time.LocalDate;

public record EventMetricResponse(
        Long eventId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        long participants,
        long donatedClothes,
        long exchangedClothes
) {
}
