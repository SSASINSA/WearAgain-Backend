package com.ssasinsa.wearagain.domain.event.dto.admin;

import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 행사 상태 변경 요청")
public record EventAdminStatusUpdateRequest(
        @Schema(description = "변경할 상태", example = "OPEN")
        @NotNull
        EventStatus status,

        @Schema(description = "상태 변경 메모", example = "검수 완료")
        String memo
) {
}
