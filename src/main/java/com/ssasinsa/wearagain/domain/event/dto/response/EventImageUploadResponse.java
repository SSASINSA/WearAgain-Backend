package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 이미지 업로드 응답")
public record EventImageUploadResponse(
        @Schema(description = "저장된 이미지 경로", example = "events/20251103/1a2b3c4d5e6f7g8h9i0j.jpg")
        String imageName,

        @Schema(description = "이미지 접근 URL", example = "https://admin.wearagain.kr/uploads/events/20251103/1a2b3c4d5e6f7g8h9i0j.jpg")
        String imageUrl
) {
}
