package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "QR 토큰 발급 응답")
public record EventApplicationQrResponse(
        @Schema(description = "QR 토큰", example = "3b3f6e3456d34a84b41ce8a3f7fb16b1")
        String qrToken,

        @Schema(description = "남은 유효 시간(초)", example = "600")
        int expiresIn
) {
}
