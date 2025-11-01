package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;

public record AdminSignupRequestResponse(
        Long signupRequestId,
        AdminSignupRequestStatus status,
        String message
) {

    public static AdminSignupRequestResponse pending(AdminSignupRequest request) {
        return new AdminSignupRequestResponse(
                request.getId(),
                request.getStatus(),
                "가입 신청이 접수되었습니다. 승인을 기다려 주세요."
        );
    }
}
