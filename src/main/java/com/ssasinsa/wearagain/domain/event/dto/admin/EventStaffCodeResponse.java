package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "행사 스태프 코드 응답")
public record EventStaffCodeResponse(
        @Schema(description = "행사 ID", example = "101")
        Long eventId,

        @Schema(description = "6자리 스태프 코드", example = "023941")
        String staffCode,

        @Schema(description = "코드 발급 시각(UTC)", example = "2025-02-01T10:15:20Z")
        OffsetDateTime issuedAt
) {

    public static EventStaffCodeResponse of(Long eventId, String staffCode, OffsetDateTime issuedAt) {
        return new EventStaffCodeResponse(eventId, staffCode, issuedAt);
    }
}
