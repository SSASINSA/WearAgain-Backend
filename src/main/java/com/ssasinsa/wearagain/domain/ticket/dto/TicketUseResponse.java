package com.ssasinsa.wearagain.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "교환 티켓 사용 응답")
public record TicketUseResponse(
        @Schema(description = "차감 전 티켓 잔여량", example = "3")
        int ticketCountBefore,

        @Schema(description = "차감 후 티켓 잔여량", example = "2")
        int ticketCountAfter,

        @Schema(description = "사용 처리 시각(UTC)", example = "2025-02-10T09:05:12Z")
        OffsetDateTime usedAt
) {
}
