package com.ssasinsa.wearagain.domain.event.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record EventCreateResponse(
        Long eventId,
        String title,
        String description,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String applyUrl,
        List<EventCreateImageResponse> images,
        List<EventCreateOptionResponse> options,
        OffsetDateTime createdAt
) {

    public record EventCreateImageResponse(
            Long eventImageId,
            String url,
            String altText,
            int displayOrder
    ) {
    }

    public record EventCreateOptionResponse(
            Long eventOptionId,
            String name,
            String type,
            int displayOrder,
            Integer capacity,
            List<EventCreateOptionResponse> children
    ) {
    }
}
