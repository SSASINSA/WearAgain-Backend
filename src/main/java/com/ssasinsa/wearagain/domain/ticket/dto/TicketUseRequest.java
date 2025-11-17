package com.ssasinsa.wearagain.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "교환 티켓 사용 요청")
public record TicketUseRequest(
        @Schema(description = "QR 토큰", example = "ae9e35d24f574c9b8a5b6b1d45e285ec")
        @NotBlank
        String qrToken,

        @Schema(description = "스태프 코드", example = "023941")
        @NotBlank
        String code,

        @Schema(description = "차감 수량", example = "1")
        @Min(1)
        int amount
) {
}
