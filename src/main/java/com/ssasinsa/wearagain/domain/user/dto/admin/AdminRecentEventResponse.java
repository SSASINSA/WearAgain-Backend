package com.ssasinsa.wearagain.domain.user.dto.admin;

import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminRecentEventResponse(
        Long eventId,
        String title,
        String thumbnailUrl,
        EventApplicationStatus status,
        LocalDate startDate,
        LocalDate endDate,
        OffsetDateTime appliedAt
) {
}
