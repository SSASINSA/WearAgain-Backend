package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "스토어 주문 요약 응답")
public record StoreOrderSummaryResponse(
        @Schema(description = "주문 ID", example = "501")
        Long orderId,
        @Schema(description = "아이템 ID", example = "101")
        Long itemId,
        @Schema(description = "아이템명", example = "웰컴 티셔츠")
        String itemName,
        @Schema(description = "주문 수량", example = "2")
        int quantity,
        @Schema(description = "단가", example = "1200")
        int unitPrice,
        @Schema(description = "사용 크레딧", example = "2400")
        int usedCredit,
        @Schema(description = "픽업 장소", example = "강남 팝업스토어")
        String pickupLocation,
        @Schema(description = "주문 상태", example = "CANCELED")
        StoreOrderStatus status,
        @Schema(description = "구매 시각 (UTC)", example = "2025-02-11T04:00:00Z")
        OffsetDateTime purchasedAt,
        @Schema(description = "취소 시각 (UTC)", example = "2025-02-11T04:10:00Z")
        OffsetDateTime canceledAt
) {
}
