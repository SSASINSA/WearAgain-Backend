package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 가입 신청 생성 응답")
public record AdminSignupRequestResponse(
        @Schema(description = "가입 신청 ID", example = "10")
        Long signupRequestId,

        @Schema(description = "가입 신청 상태", example = "PENDING")
        AdminSignupRequestStatus status,

        @Schema(description = "안내 메시지", example = "가입 신청이 접수되었습니다. 승인을 기다려 주세요.")
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
