package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 이미지 업로드 응답")
public record EventImageUploadResponse(
        @Schema(description = "저장된 이미지 이름", example = "6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg")
        String imageName,

        @Schema(description = "이미지 접근 URL", example = "https://admin.wearagain.kr/uploads/6f7e4a1b2c3d4e5f6a7b8c9d0e1f2a3b.jpg")
        String imageUrl
) {
}
