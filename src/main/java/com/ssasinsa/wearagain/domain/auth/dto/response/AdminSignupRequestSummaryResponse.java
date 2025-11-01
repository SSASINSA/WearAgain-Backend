package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import java.time.LocalDateTime;

public record AdminSignupRequestSummaryResponse(
        Long signupRequestId,
        String email,
        String name,
        AdminRole requestedRole,
        AdminSignupRequestStatus status,
        String reason,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
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

    public record Reviewer(Long adminId, String name) {

        private static Reviewer from(AdminUser reviewer) {
            if (reviewer == null) {
                return null;
            }
            return new Reviewer(reviewer.getId(), reviewer.getName());
        }
    }
}
