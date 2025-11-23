package com.ssasinsa.wearagain.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토어 상품 이미지 업로드 응답")
public record StoreImageUploadResponse(
        @Schema(description = "저장된 이미지 이름", example = "store/20251123/abc123.jpg")
        String imageName,

        @Schema(description = "이미지 접근 URL", example = "https://api.wearagain.kr/upload/store/20251123/abc123.jpg")
        String imageUrl
) {
}
