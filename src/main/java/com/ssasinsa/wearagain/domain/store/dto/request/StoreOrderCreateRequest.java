package com.ssasinsa.wearagain.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "스토어 주문 생성 요청")
public record StoreOrderCreateRequest(
        @Schema(description = "아이템 ID", example = "101")
        @NotNull
        Long itemId,

        @Schema(description = "수량", example = "2")
        @Min(1)
        int quantity,

        @Schema(description = "픽업 장소", example = "강남 팝업스토어")
        @NotBlank
        @Size(max = 255)
        String pickupLocation
) {
}
