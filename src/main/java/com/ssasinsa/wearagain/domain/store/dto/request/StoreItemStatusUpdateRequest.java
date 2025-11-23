package com.ssasinsa.wearagain.domain.store.dto.request;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스토어 상품 상태 변경 요청")
public record StoreItemStatusUpdateRequest(
        @Schema(description = "상품 상태", example = "ACTIVE")
        @NotNull
        StoreItemStatus status
) {
}
