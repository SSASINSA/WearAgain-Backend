package com.ssasinsa.wearagain.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 가입 신청 목록 응답")
public record AdminSignupRequestListResponse(
        @Schema(description = "가입 신청 목록")
        List<AdminSignupRequestSummaryResponse> items
) {

    public static AdminSignupRequestListResponse of(List<AdminSignupRequestSummaryResponse> items) {
        return new AdminSignupRequestListResponse(List.copyOf(items));
    }
}
