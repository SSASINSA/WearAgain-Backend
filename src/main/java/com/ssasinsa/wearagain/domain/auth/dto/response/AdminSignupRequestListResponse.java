package com.ssasinsa.wearagain.domain.auth.dto.response;

import java.util.List;

public record AdminSignupRequestListResponse(
        List<AdminSignupRequestSummaryResponse> items
) {

    public static AdminSignupRequestListResponse of(List<AdminSignupRequestSummaryResponse> items) {
        return new AdminSignupRequestListResponse(List.copyOf(items));
    }
}
