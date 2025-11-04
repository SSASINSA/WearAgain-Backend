package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 가입 신청 요약 응답")
public record AdminSignupRequestSummaryResponse(
        @Schema(description = "가입 신청 ID", example = "10")
        Long signupRequestId,

        @Schema(description = "신청자 이메일", example = "candidate@wearagain.kr")
        String email,

        @Schema(description = "신청자 이름", example = "관리자 후보")
        String name,

        @Schema(description = "요청 역할", example = "ADMIN")
        AdminRole requestedRole,

        @Schema(description = "신청 상태", example = "PENDING")
        AdminSignupRequestStatus status,

        @Schema(description = "신청 사유", example = "운영팀 신규 인력")
        String reason,

        @Schema(description = "거절 사유", example = "서류 미비", nullable = true)
        String rejectionReason,

        @Schema(description = "신청 일시", example = "2025-11-03T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "검토 일시", example = "2025-11-03T12:00:00", nullable = true)
        LocalDateTime reviewedAt,

        @Schema(description = "검토자 정보", nullable = true)
        Reviewer reviewer
) {

    public static AdminSignupRequestSummaryResponse from(AdminSignupRequest request) {
        return new AdminSignupRequestSummaryResponse(
                request.getId(),
                request.getEmail(),
                request.getName(),
                request.getRequestedRole(),
                request.getStatus(),
                request.getReason(),
                request.getRejectionReason(),
                request.getCreatedAt(),
                request.getReviewedAt(),
                Reviewer.from(request.getReviewedBy())
        );
    }

    @Schema(description = "검토자 정보")
    public record Reviewer(
            @Schema(description = "검토자 ID", example = "1")
            Long adminId,

            @Schema(description = "검토자 이름", example = "슈퍼관리자")
            String name
    ) {

        private static Reviewer from(AdminUser reviewer) {
            if (reviewer == null) {
                return null;
            }
            return new Reviewer(reviewer.getId(), reviewer.getName());
        }
    }
}
