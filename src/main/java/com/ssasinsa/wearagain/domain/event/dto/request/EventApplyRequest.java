package com.ssasinsa.wearagain.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "행사 신청 요청")
public record EventApplyRequest(
        @NotNull
        @Schema(description = "선택한 행사 옵션 ID", example = "2003", requiredMode = Schema.RequiredMode.REQUIRED)
        Long optionId,

        @Size(max = 255)
        @Schema(description = "추가 메모(선택)", example = "동행 1인 포함")
        String memo
) {
}
