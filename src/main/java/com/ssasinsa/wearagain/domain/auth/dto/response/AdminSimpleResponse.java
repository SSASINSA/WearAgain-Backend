package com.ssasinsa.wearagain.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 간단 응답")
public record AdminSimpleResponse(
        @Schema(description = "응답 메시지", example = "요청이 정상 처리되었습니다.")
        String message
) {

    public static AdminSimpleResponse of(String message) {
        return new AdminSimpleResponse(message);
    }
}
