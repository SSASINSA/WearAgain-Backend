package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "스토어 주문 상세 응답")
public record StoreOrderDetailResponse(
        @Schema(description = "주문 ID", example = "501")
        Long orderId,
        @Schema(description = "아이템 정보")
        OrderItem item,
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

    @Schema(description = "주문 아이템 정보")
    public record OrderItem(
            @Schema(description = "아이템 ID", example = "101")
            Long id,
            @Schema(description = "아이템명", example = "웰컴 티셔츠")
            String name,
            @Schema(description = "단가", example = "1200")
            Integer price,
            @Schema(description = "이미지 목록")
            List<StoreItemDetailResponse.StoreItemImageResponse> images
    ) {
    }
}
