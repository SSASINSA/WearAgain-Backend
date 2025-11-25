package com.ssasinsa.wearagain.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 요약 정보 응답")
public record UserSummaryResponse(
        @Schema(description = "사용자 표시 이름", example = "김웨어")
        String displayName,

        @Schema(description = "현재 보유 교환 티켓", example = "3")
        int ticketBalance,

        @Schema(description = "현재 보유 크레딧", example = "120")
        int creditBalance,

        @Schema(description = "교환 티켓 증감 총합 (충전-사용)", example = "15")
        int totalTicketChangeAmount
) {
}
