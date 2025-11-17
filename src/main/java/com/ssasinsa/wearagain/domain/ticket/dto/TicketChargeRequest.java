package com.ssasinsa.wearagain.domain.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "교환 티켓 충전 요청")
public record TicketChargeRequest(
        @Schema(description = "충전 대상 사용자 ID", example = "1")
        @NotNull
        Long userId,

        @Schema(description = "충전 수량", example = "5")
        @Min(1)
        int amount,

        @Schema(description = "충전 사유", example = "이벤트 보상")
        @NotBlank
        @Size(max = 255)
        String reason
) {
}
