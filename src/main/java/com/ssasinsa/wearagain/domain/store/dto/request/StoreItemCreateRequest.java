package com.ssasinsa.wearagain.domain.store.dto.request;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "스토어 상품 등록 요청")
public record StoreItemCreateRequest(
        @Schema(description = "상품명", example = "에코백")
        @NotBlank
        @Size(min = 1, max = 255)
        String name,

        @Schema(description = "상품 설명", example = "재활용 원단으로 제작한 친환경 에코백입니다.")
        @Size(max = 4000)
        String description,

        @Schema(description = "카테고리", example = "bag")
        @Size(max = 50)
        String category,

        @Schema(description = "판매가", example = "12000")
        @Positive
        int price,

        @Schema(description = "재고 수량", example = "100")
        @PositiveOrZero
        Integer stock,

        @Schema(description = "상품 상태", example = "ACTIVE")
        StoreItemStatus status,

        @Schema(description = "상품 이미지 목록(최대 10개)")
        @Size(max = 10)
        List<@Valid StoreItemImageRequest> images
) {

    @Schema(description = "스토어 상품 이미지 정보")
    public record StoreItemImageRequest(
            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/store/items/1/main.jpg")
            @NotBlank
            @Size(min = 1, max = 512)
            String imageUrl,

            @Schema(description = "노출 순서(1부터 시작)", example = "1")
            @NotNull
            @Positive
            Integer sortOrder
    ) {
    }
}
