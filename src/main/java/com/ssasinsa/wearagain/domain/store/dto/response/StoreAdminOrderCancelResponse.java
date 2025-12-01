package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "어드민 주문 취소 응답")
public record StoreAdminOrderCancelResponse(
        @Schema(description = "주문 ID", example = "1001")
        Long orderId,
        @Schema(description = "주문 상태", example = "CANCELED")
        StoreOrderStatus status,
        @Schema(description = "환불 금액", example = "4000")
        int refundedAmount,
        @Schema(description = "취소 시각(UTC)", example = "2025-02-11T05:00:00Z")
        OffsetDateTime canceledAt
) {
}
