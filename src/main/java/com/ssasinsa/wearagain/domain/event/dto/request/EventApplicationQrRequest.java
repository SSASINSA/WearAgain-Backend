package com.ssasinsa.wearagain.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "QR 토큰 발급 요청")
public record EventApplicationQrRequest(
        @Schema(description = "기존 토큰을 강제로 재발급할지 여부", example = "false")
        Boolean forceReissue
) {

    public boolean isForceReissue() {
        return Boolean.TRUE.equals(forceReissue);
    }
}
