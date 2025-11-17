package com.ssasinsa.wearagain.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "교환 티켓 충전 응답")
public record TicketChargeResponse(
        @Schema(description = "충전 전 티켓 잔여량", example = "2")
        int ticketCountBefore,

        @Schema(description = "충전 후 티켓 잔여량", example = "7")
        int ticketCountAfter,

        @Schema(description = "충전 처리 시각(UTC)", example = "2025-02-11T10:00:00Z")
        OffsetDateTime chargedAt
) {
}
