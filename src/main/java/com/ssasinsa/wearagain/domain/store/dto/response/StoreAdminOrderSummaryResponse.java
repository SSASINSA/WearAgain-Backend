package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "어드민용 스토어 주문 요약 정보")
public record StoreAdminOrderSummaryResponse(
        @Schema(description = "주문 ID", example = "1001")
        Long orderId,
        @Schema(description = "사용자 ID", example = "2001")
        Long userId,
        @Schema(description = "사용자 이메일", example = "admin@wearagain.com")
        String userEmail,
        @Schema(description = "상품 ID", example = "501")
        Long itemId,
        @Schema(description = "상품명", example = "웨어어게인 굿즈")
        String itemName,
        @Schema(description = "주문 수량", example = "2")
        int quantity,
        @Schema(description = "상품 단가", example = "2000")
        int unitPrice,
        @Schema(description = "총 결제 금액", example = "4000")
        int totalPrice,
        @Schema(description = "픽업 위치", example = "서울 본점")
        String pickupLocation,
        @Schema(description = "주문 상태", example = "PURCHASED")
        StoreOrderStatus status,
        @Schema(description = "구매 시각(UTC)", example = "2025-02-11T04:00:00Z")
        OffsetDateTime purchasedAt,
        @Schema(description = "취소 시각(UTC)", example = "2025-02-12T09:00:00Z")
        OffsetDateTime canceledAt
) {
}
