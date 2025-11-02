package com.ssasinsa.wearagain.domain.event.dto.request;

import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record EventCreateRequest(
        @NotBlank
        @Size(min = 1, max = 100)
        String title,
        @NotBlank
        @Size(min = 10, max = 2000)
        String description,
        @NotBlank
        @Size(min = 1, max = 255)
        String location,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate,
        EventStatus status,
        @NotEmpty
        @Size(max = 10)
        List<@Valid EventCreateImageRequest> images,
        List<@Valid EventCreateOptionRequest> options
) {

    public record EventCreateImageRequest(
            @NotBlank
            @Size(min = 1, max = 1024)
            String url,
            @Size(max = 255)
            String altText,
            @Positive
            int displayOrder
    ) {
    }

    public record EventCreateOptionRequest(
            @NotBlank
            @Size(min = 1, max = 100)
            String name,
            @NotBlank
            @Size(min = 1, max = 20)
            String type,
            @Positive
            int displayOrder,
            Integer capacity,
            List<@Valid EventCreateOptionRequest> children
    ) {
    }
}
