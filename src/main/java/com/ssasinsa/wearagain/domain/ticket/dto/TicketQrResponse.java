package com.ssasinsa.wearagain.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "교환 티켓 QR 응답")
public record TicketQrResponse(
        @Schema(description = "잔여 교환 티켓 수량", example = "5")
        int ticketCount,

        @Schema(description = "QR 토큰", example = "ae9e35d24f574c9b8a5b6b1d45e285ec")
        String ticketToken,

        @Schema(description = "토큰 만료까지 남은 시간(초)", example = "900")
        int ticketTokenExpiresIn
) {
}
