package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "스토어 주문 취소 응답")
public record StoreOrderCancelResponse(
        @Schema(description = "주문 ID", example = "501")
        Long orderId,
        @Schema(description = "주문 상태", example = "CANCELED")
        StoreOrderStatus status,
        @Schema(description = "환불 크레딧", example = "2400")
        int refundedCredit,
        @Schema(description = "취소 시각 (UTC)", example = "2025-02-11T04:10:00Z")
        OffsetDateTime canceledAt
) {
}
