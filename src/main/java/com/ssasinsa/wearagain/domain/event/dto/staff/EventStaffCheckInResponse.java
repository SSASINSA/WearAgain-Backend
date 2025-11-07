package com.ssasinsa.wearagain.domain.event.dto.staff;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "행사 스태프 체크인 응답")
public record EventStaffCheckInResponse(
        @Schema(description = "신청 ID", example = "123")
        Long applicationId,

        @Schema(description = "체크인 후 상태", example = "CHECKED_IN")
        String status,

        @Schema(description = "체크인 완료 시각", example = "2025-02-10T09:05:12Z")
        OffsetDateTime checkedInAt,

        @Schema(description = "참가자 이름", example = "홍길동")
        String userDisplayName,

        @Schema(description = "행사 제목", example = "업사이클링 원데이 클래스")
        String eventTitle
) {
}
